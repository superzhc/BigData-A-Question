DataX 在设置 Hive 的分隔符的时候，只支持单字符的设置，多字符会报错

以下这些字符是可以在 Hive 中应用的，并且 DataX 也支持的：

| char | digraph | hex  | dec  | official name             | Unicode |
| ---- | ------- | ---- | ---- | ------------------------- | ------- |
| ^@   | NU      | 0x00 | 0    | NULL (NUL)N               | \u0000  |
| ^A   | SH      | 0x01 | 1    | START OF HEADING (SOH)    | \u0001  |
| ^B   | SX      | 0x02 | 2    | START OF TEXT (STX)       | \u0002  |
| ^C   | EX      | 0x03 | 3    | END OF TEXT (ETX)         | \u0003  |
| ^D   | ET      | 0x04 | 4    | END OF TRANSMISSION (EOT) | \u0004  |
| ^E   | EQ      | 0x05 | 5    | ENQUIRY (ENQ)             | \u0005  |
| ^F   | AK      | 0x06 | 6    | ACKNOWLEDGE (ACK)         | \u0006  |
| ^G   | BL      | 0x07 | 7    | BELL (BEL)                | \u0007  |
| ^H   | BS      | 0x08 | 8    | BACKSPACE (BS)            | \u0008  |
| ^I   | HT      | 0x09 | 9    | CHARACTER TABULATION (HT) | \u0009  |
| ^@   | LF      | 0x0a | 10   | LINE FEED (LF)            | \u0010  |
| ^K   | VT      | 0x0b | 11   | LINE TABULATION (VT)      | \u0011  |
| ^L   | FF      | 0x0c | 12   | FORM FEED (FF)            | \u0012  |
| ^M   | CR      | 0x0d | 13   | CARRIAGE RETURN (CR)      | \u0013  |

**注意：特殊符号中的^ 和键盘上的^ 字符是不一样的。另外特殊符号中的^和后面跟的字符是一体的，也就是说，两个字符是一个符号。**

