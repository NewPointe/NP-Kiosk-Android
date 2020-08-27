package org.newpointe.kiosk.models.card

enum CardSource {
    /** ATM card feeder.*/
    ATM = 0,

    /** Main card feeder.*/
    Feeder = 1,

    /** Internal to the printer.*/
    Internal = 2,

    /** Main feeder or ATM. For ZMotif Series 7 and ZC Series printers only. */
    AutoDetect = 3
}
