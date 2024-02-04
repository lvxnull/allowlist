package org.lvxnull.allowlist;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AllowListUtil {
    private AllowListUtil() {}

    private static final Pattern formatCodePattern = Pattern.compile("&([0-9a-fk-or<>]|#[0-9a-fA-F]{6})");
    public static MutableText colorize(String msg) {
        MutableText builder = Text.empty();
        Style style = Style.EMPTY;
        Matcher m = formatCodePattern.matcher(msg);

        int start = 0;
        boolean escaped = false;
        while(m.find()) {
            String code = m.group(1);
            if(escaped && code.charAt(0) != '>') continue;

            if(start < m.start()) {
                MutableText t = Text.literal(msg.substring(start, m.start()));
                t.fillStyle(style);
                builder.append(t);
            }

            switch(code.charAt(0)) {
                case '#':
                    style = style.withColor(Integer.parseInt(code.substring(1), 16));
                    break;
                case '<':
                    escaped = true;
                    break;
                case '>':
                    escaped = false;
                    break;
                default:
                    style = style.withFormatting(Formatting.byCode(code.charAt(0)));
            }

            start = m.end();
        }

        if(start < msg.length()) {
            MutableText t = Text.literal(msg.substring(start));
            t.fillStyle(style);
            builder.append(t);
        }

        return builder;
    }

    public static MutableText colorize(String msg, Object... args) {
        return colorize(String.format(msg, args));
    }
}
