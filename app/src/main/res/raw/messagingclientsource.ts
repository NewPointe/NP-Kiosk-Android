declare interface Window {
    hasRockCheckinClientAPI?: boolean;
    ZebraPrintPlugin?: {
        printTags?: (labelJson: any, success: () => void, fail: () => void) => void;
    };
    Cordova?: {
        exec?: (success: () => void, fail: () => void, classname: string, method: string, args: any[]) => void;
    };
    labelData?: any;
    onDeviceReady?: () => void;
}
declare interface External {
    PrintLabels?: (labels: any) => void;
}

(() => {

    // Guard in case we're injected more than once
    if (window.hasRockCheckinClientAPI) return;
    window.hasRockCheckinClientAPI = true;

    interface RpcRequest<T extends Array<unknown> | Record<string, unknown>> {
        jsonrpc: "2.0";
        method: string;
        params?: T;
        id?: string | number | null;
    }

    interface RpcResponseSuccess<T> {
        jsonrpc: "2.0";
        result?: T;
        id: string | number | null;
    }

    interface RpcResponseError<T> {
        jsonrpc: "2.0";
        error?: RpcError<T>;
        id: string | number | null;
    }

    type RpcResponse<T> = RpcResponseSuccess<T> | RpcResponseError<T>;

    interface RpcError<T> {
        code: number;
        message: string;
        data?: T;
    }

    interface ClientApi {
        print(input: string): string;
        getAppPreference(key: string): string | null;
        setAppPreference(key: string, value: string | null): Boolean;
        showSettings(): void;
    }

    type AnyFunction = (...args: any[]) => any;
    type Promisify<T extends AnyFunction> = (...args: Parameters<T>) => Promise<ReturnType<T>>;
    type PromisifyAny<T> = T extends AnyFunction ? Promisify<T> : never;
    type Proxy<T> = { [TKey in keyof T]: PromisifyAny<T[TKey]> }
    type ClientApiProxy = Proxy<ClientApi>

    interface PromiseCache {
        resolve: AnyFunction,
        reject: AnyFunction
    }

    abstract class AbstractRpcClient {

        private callbacks = new Map<string, PromiseCache>();

        public notify<T extends any[]>(method: string, params: T) {
            this.internalWrite(JSON.stringify({ jsonrpc: "2.0", method, params } as RpcRequest<T>));
        }

        public write<T extends any[]>(method: string, params: T): Promise<unknown> {
            const id = generateGuid();
            const callback = new Promise((resolve, reject) => this.callbacks.set(id, { resolve, reject }));
            this.internalWrite(JSON.stringify({ jsonrpc: "2.0", method, params, id } as RpcRequest<T>));
            return callback;
        }

        protected abstract internalWrite(message: string): void;

        protected messageReceived(message: string) {
            const response = tryParse<RpcResponse<unknown>>(message);
            if (response === null) {
                console.error("[RpcClient] Failed to parse JSON data: " + message);
            }
            else {
                if (response.id) {
                    const id = response.id.toString();
                    const callback = this.callbacks.get(id);
                    if (callback) {
                        this.callbacks.delete(id);
                        if ('error' in response) {
                            callback.reject(new Error(response.error?.message));
                        }
                        else if ('result' in response) {
                            callback.resolve(response.result);
                        }
                    }
                }
            }
        }
    }

    class RpcWebMessageClient<T> extends AbstractRpcClient {

        public readonly rpc: Proxy<T> = new Proxy(Object.create(null), { get: (_: T, property: PropertyKey, __: any) => (...params: any[]) => this.write(property.toString(), params) });

        constructor(private readonly messagePort: MessagePort) {
            super();
            messagePort.addEventListener("message", this.OnPortMessage.bind(this));
            messagePort.addEventListener("messageerror", this.OnPortMessageError.bind(this));
        }

        private OnPortMessage(ev: MessageEvent) {
            this.messageReceived(ev.data);
        }

        private OnPortMessageError(ev: MessageEvent) {
            console.log("[RpcClient] WebMessage Error: " + ev.data);
        }

        protected internalWrite(message: string) {
            this.messagePort.postMessage(message);
        }

    }

    /**
     * Lookup table for hex conversion
     */
    const hex = Array(256).fill(0).map((_, i) => i.toString(16).padStart(2, '0'));

    /**
     * Generates a random GUID
     */
    function generateGuid() {
        const b = new Uint8Array(16);
        window.crypto.getRandomValues(b);
        return hex[b[0]] + hex[b[1]] + hex[b[2]] + hex[b[3]] + '-' + hex[b[4]] + hex[b[5]] + '-' + hex[b[6] | 0x40] + hex[b[7]] + '-' + hex[b[8] | 0x80] + hex[b[9]] + '-' + hex[b[10]] + hex[b[11]] + hex[b[12]] + hex[b[13]] + hex[b[14]] + hex[b[15]];
    }

    let client: RpcWebMessageClient<ClientApiProxy> | null = null;

    /**
     * Tries to parse a JSON string
     * @param {string} data The data to parse
     */
    function tryParse<T>(data: string) {
        try { return JSON.parse(data) as T; }
        catch (e) { return null; }
    }

    // Set up the API

    // Windows client compatibility shim
    if (!window.external) (window.external as any) = {};
    window.external.PrintLabels = (labelsJson) => {
        client?.rpc.print(JSON.parse(labelsJson));
    };

    // iOS client compatibility shim
    if (!window.Cordova) window.Cordova = {};
    window.Cordova.exec = (success, fail, classname, method, args) => {
        if (classname === "ZebraPrint" && method === "printTags") {
            client?.rpc.print(JSON.parse(args[0])).then(success, fail);
        }
        else if (classname === "ApplicationPreferences") {
            if (method === "getSetting") {
                client?.rpc.getAppPreference(args[0].key).then(success, fail);
            }
            else if (method === "setSetting") {
                client?.rpc.setAppPreference(args[0].key, args[0].value).then(success, fail);
            }
        }
    };

    // ZebraPrintPlugin compatibility shim
    if (!window.ZebraPrintPlugin) window.ZebraPrintPlugin = {};
    window.ZebraPrintPlugin.printTags = (labelJson, success, fail) => {
        client?.rpc.print(JSON.parse(labelJson)).then(success, fail);
    };

    console.log('[NP Check-in] Starting web messaging client');

    window.addEventListener("message", (ev: MessageEvent) => {
        if (ev.data === "org.newpointe.kiosk.CHECKIN_API_INIT" && ev.ports.length === 1) {
            console.log('[NP Check-in] Got Init message');
            client = new RpcWebMessageClient(ev.ports[0]);
        };
        // Check if the label data already exists, in which case we got injected after the printing code and need to repeat it ourselves
        if (window.labelData && window.onDeviceReady) {
            window.onDeviceReady();
            window.onDeviceReady = () => { };
        }
    }, false);

})();
