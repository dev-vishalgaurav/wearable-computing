package edu.dartmouth.cs.watchacccollector.classifier;

public class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N5ae495f74(i);
        return p;
    }

    static double N5ae495f74(Object[] i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() <= 0.981109) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() > 0.981109) {
            p = WekaClassifier.N334f16055(i);
        }
        return p;
    }

    static double N334f16055(Object[] i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 785.160523) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() > 785.160523) {
            p = WekaClassifier.N324d79796(i);
        }
        return p;
    }

    static double N324d79796(Object[] i) {
        double p = Double.NaN;
        if (i[7] == null) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() <= 39.428201) {
            p = WekaClassifier.N3c268b167(i);
        } else if (((Double) i[7]).doubleValue() > 39.428201) {
            p = 2;
        }
        return p;
    }

    static double N3c268b167(Object[] i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 928.141953) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() > 928.141953) {
            p = 2;
        }
        return p;
    }
}