CALL apoc.custom.installFunction(
    'parseIsoOffsetDateTime(time :: STRING, unit = ms :: STRING) :: (INTEGER?)',
    'WITH apoc.text.regreplace($time, "(\.\d{1,3})\d*", "$1") AS time // limit fraction to 3 digits because parsing pattern only supports milliseconds (see java.text.SimpleDateFormat)
    WITH apoc.text.regreplace(time, "(T\d{2}:\d{2})([+-]\d{2}:\d{2}|Z)$", "$1:00$2") AS time // insert seconds block if missing because parsing pattern does not support optional sections (see java.text.SimpleDateFormat)
    WITH apoc.text.regreplace(time, "(T\d{2}:\d{2}:\d{2})([+-]\d{2}:\d{2}|Z)$", "$1.0$2") AS time // insert milliseconds block if missing because parsing pattern does not support optional sections (see java.text.SimpleDateFormat)
    RETURN apoc.date.parse(time, $unit, "yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX")',
    'orkg',
    false,
    'custom.parseIsoOffsetDateTime(\'2012-12-23T21:15:05.645313+02:00\', \'ms|s|m|h|d\') - parse date string as offset date time into the specified time unit'
);
