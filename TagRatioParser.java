package org.apache.tika.parser;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.Set;

public class TagRatioParser extends AbstractParser {

    static final AutoDetectParser parser = new AutoDetectParser();
    static final StandardDeviation standardDeviation = new StandardDeviation();
    static final Mean mean = new Mean();

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return null;
    }

    @Override
    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
        ContentHandler xmlHandler = new ToXMLContentHandler();
        parser.parse(stream, xmlHandler, metadata, context);
        String xhtmlContent  = xmlHandler.toString();
        char content[] = getContentViaTagRatio(xhtmlContent);
        handler.characters(content, 0, content.length);
    }

    char[] getContentViaTagRatio(String xhtml) {
        String lines[] = xhtml.split("\n");
        double TTR[] = new double[lines.length];
        for (int i=0;i<lines.length; i++) {
            double lineValues[] = analyzeLine(lines,i);
            TTR[i] = lineValues[0] / lineValues [1];
        }
        TTR = smoothing(TTR);
        double sd = standardDeviation.evaluate(TTR);
        double avg = mean.evaluate(TTR, 0 , TTR.length);
//        System.out.println("Avg:"+avg+" SD:"+ sd);

        double threshold = avg + sd;

        String extractedContent = extractContent(TTR, lines, threshold);
        return extractedContent.toCharArray();

    }

    String extractContent(double[] TTR, String lines[], double threshold) {
        String content = "";
        for(int i=0;i<TTR.length;i++) {
            if(TTR[i] >= threshold && !lines[i].trim().equals("")) {
                content+=lines[i];
                content+="\n";
            }
        }
        return content;
    }

    double[] smoothing(double[] TTR) {
        double  newTTR[] = new double[TTR.length];
        int r=2;
        for(int i=0;i<TTR.length;i++) {
            newTTR[i] = TTR[i];
            while(r!=0) {
                if(i+r < TTR.length)
                    newTTR[i]+=TTR[i+r];
                if(i-r > 0)
                    newTTR[i]+=TTR[i-r];
                r-=1;
            }
            r=2;
            newTTR[i] = newTTR[i] / (2*r + 1);
        }
        return newTTR;
    }

    double[] analyzeLine (String[] lines, int index) {
        double ret[] = {0,1};
        if(lines[index].isEmpty() || lines[index].equals(""))
            return ret;
        double x = 0, y = 0;
        int i = 0;
        String content="";
        while(i < lines[index].length()) {
            if(lines[index].charAt(i) == '<') {
                int old_i =i;
                while (i<lines[index].length() - 1 && lines[index].charAt(++i) != '>');
                if(i == lines[index].length())
                    x=x+lines[index].length()-old_i;
                else {
                    y += 1; //increment tag count by one
                    i += 1;
                }
            } else {
                content+=lines[index].charAt(i);
                x += 1;
                i += 1;
            }
        }
        lines[index] = content;
        ret[0] = x;
        if(y!=0)
            ret[1] = y;
        return ret;
    }
}
