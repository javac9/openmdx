// ** I18N

// Calendar IT language
// Author: Devis Lucato, <devis@lucato.it>
// Encoding: UTF8
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("Domenica",
 "Lunedi'",
 "Martedi'",
 "Mercoledi'",
 "Giovedi'",
 "Venerdi'",
 "Sabato",
 "Domenica");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("Dom",
 "Lun",
 "Mar",
 "Mer",
 "Gio",
 "Ven",
 "Sab",
 "Dom");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 1;

// full month names
Calendar._MN = new Array
("Gennaio",
 "Febbraio",
 "Marzo",
 "Aprile",
 "Maggio",
 "Giugno",
 "Luglio",
 "Agosto",
 "Settembre",
 "Ottobre",
 "Novembre",
 "Dicembre");

// short month names
Calendar._SMN = new Array
("Gen",
 "Feb",
 "Mar",
 "Apr",
 "Mag",
 "Giu",
 "Lug",
 "Ago",
 "Set",
 "Ott",
 "Nov",
 "Dic");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Info sul calendario";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Autore: Mihai Bazon\n" + // don't translate this this ;-)
"For latest version visit: http://www.dynarch.com/projects/calendar/\n" +
"Distributed under GNU LGPL.  See http://gnu.org/licenses/lgpl.html for details." +
"\n\n" +
"Scelta della data:\n" +
"- Usa i tasti \u00ab, \u00bb per scegliere l'anno\n" +
"- Usa i tasti \u2039, \u203a per scegliere il meseh\n" +
"- Tieni premuto il tasto del mouse su bottoni per una scelta piu' veloce.";

Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Scelta dell'ora:\n" +
"- Clicca sulle parti dell'ora per incrementarla\n" +
"- Clicca tenendo il tasto Shift premuto per decrementarla\n" +
"- Clicca e trascina per una selezione più veloce.";

Calendar._TT["PREV_YEAR"] = "Anno prec. (tieni premuto per il menu)";
Calendar._TT["PREV_MONTH"] = "Mese prec. (tieni premuto per il menu)";
Calendar._TT["GO_TODAY"] = "Vai a Oggi";
Calendar._TT["NEXT_MONTH"] = "Mese succ. (tieni premuto per il menu)";
Calendar._TT["NEXT_YEAR"] = "Anno succ. (tieni premuto per il menu)";
Calendar._TT["SEL_DATE"] = "Seleziona data";
Calendar._TT["DRAG_TO_MOVE"] = "Trascina per spostare";
Calendar._TT["PART_TODAY"] = " (oggi)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Mostra %s prima";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Chiudi";
Calendar._TT["TODAY"] = "Oggi";
Calendar._TT["TIME_PART"] = "(Shift-)Click o trascina per cambiare il valore";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "ST";
Calendar._TT["TIME"] = "Ora:";
