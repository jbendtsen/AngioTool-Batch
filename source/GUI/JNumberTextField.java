package GUI;

import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class JNumberTextField extends JTextField {
   private static final char DOT = '.';
   private static final char NEGATIVE = '-';
   private static final String BLANK = "";
   private static final int DEF_PRECISION = 2;
   public static final int NUMERIC = 2;
   public static final int DECIMAL = 3;
   public static final String FM_NUMERIC = "0123456789";
   public static final String FM_DECIMAL = "0123456789.";
   private int maxLength = 0;
   private int format = 2;
   private String negativeChars = "";
   private String allowedChars = null;
   private boolean allowNegative = false;
   private int precision = 0;
   protected PlainDocument numberFieldFilter;

   public JNumberTextField() {
      this(10, 3);
   }

   public JNumberTextField(int iMaxLen) {
      this(iMaxLen, 2);
   }

   public JNumberTextField(int iMaxLen, int iFormat) {
      this.setMaxLength(iMaxLen);
      this.setFormat(iFormat);
      this.numberFieldFilter = new JNumberTextField.JNumberFieldFilter();
      super.setDocument(this.numberFieldFilter);
   }

   public void setMaxLength(int maxLen) {
      if (maxLen > 0) {
         this.maxLength = maxLen;
      } else {
         this.maxLength = 0;
      }
   }

   public int getMaxLength() {
      return this.maxLength;
   }

   @Override
   public void setEnabled(boolean enable) {
      super.setEnabled(enable);
      if (enable) {
         this.setBackground(Color.white);
         this.setForeground(Color.black);
      } else {
         this.setBackground(Color.lightGray);
         this.setForeground(Color.darkGray);
      }
   }

   @Override
   public void setEditable(boolean enable) {
      super.setEditable(enable);
      if (enable) {
         this.setBackground(Color.white);
         this.setForeground(Color.black);
      } else {
         this.setBackground(Color.lightGray);
         this.setForeground(Color.darkGray);
      }
   }

   public void setPrecision(int iPrecision) {
      if (this.format != 2) {
         if (iPrecision >= 0) {
            this.precision = iPrecision;
         } else {
            this.precision = 2;
         }
      }
   }

   public int getPrecision() {
      return this.precision;
   }

   public Number getNumber() {
      Number number = null;
      if (this.format == 2) {
         number = (Integer)Integer.parseInt(this.getText());
      } else {
         number = (Double)Double.parseDouble(this.getText());
      }

      return number;
   }

   public void setNumber(Number value) {
      this.setText(String.valueOf(value));
   }

   public int getInt() {
      return Integer.parseInt(this.getText());
   }

   public void setInt(int value) {
      this.setText(String.valueOf(value));
   }

   public float getFloat() {
      return Float.parseFloat(this.getText());
   }

   public void setFloat(float value) {
      this.setText(String.valueOf(value));
   }

   public double getDouble() {
      return Double.parseDouble(this.getText());
   }

   public void setDouble(double value) {
      this.setText(String.valueOf(value));
   }

   public int getFormat() {
      return this.format;
   }

   public void setFormat(int iFormat) {
      switch(iFormat) {
         case 2:
         default:
            this.format = 2;
            this.precision = 0;
            this.allowedChars = "0123456789";
            break;
         case 3:
            this.format = 3;
            this.precision = 2;
            this.allowedChars = "0123456789.";
      }
   }

   public void setAllowNegative(boolean b) {
      this.allowNegative = b;
      if (b) {
         this.negativeChars = "-";
      } else {
         this.negativeChars = "";
      }
   }

   public boolean isAllowNegative() {
      return this.allowNegative;
   }

   @Override
   public void setDocument(Document document) {
   }

   class JNumberFieldFilter extends PlainDocument {
      public JNumberFieldFilter() {
      }

      @Override
      public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
         String text = this.getText(0, offset) + str + this.getText(offset, this.getLength() - offset);
         if (str != null && text != null) {
            for(int i = 0; i < str.length(); ++i) {
               if ((JNumberTextField.this.allowedChars + JNumberTextField.this.negativeChars).indexOf(str.charAt(i)) == -1) {
                  return;
               }
            }

            int precisionLength = 0;
            int dotLength = 0;
            int minusLength = 0;
            int textLength = text.length();

            try {
               if (JNumberTextField.this.format == 2) {
                  Long.parseLong(text);
               } else if (JNumberTextField.this.format == 3) {
                  Double.parseDouble(text);
                  int dotIndex = text.indexOf(46);
                  if (dotIndex != -1) {
                     dotLength = 1;
                     precisionLength = textLength - dotIndex - dotLength;
                     if (precisionLength > JNumberTextField.this.precision) {
                        return;
                     }
                  }
               }
            } catch (Exception var10) {
               return;
            }

            if (text.startsWith("-")) {
               if (!JNumberTextField.this.allowNegative) {
                  return;
               }

               minusLength = 1;
            }

            if (JNumberTextField.this.maxLength >= textLength - dotLength - precisionLength - minusLength) {
               super.insertString(offset, str, attr);
            }
         }
      }
   }
}
