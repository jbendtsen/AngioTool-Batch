package GUI;

import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;

public class MyHTMLEditorKit extends HTMLEditorKit {
   private static Class<?> c;

   @Override
   public ViewFactory getViewFactory() {
      return new MyHTMLEditorKit.HTMLFactoryX();
   }

   public void setJar(Class<?> c) {
      MyHTMLEditorKit.c = c;
   }

   public static class HTMLFactoryX extends HTMLFactory implements ViewFactory {
      @Override
      public View create(Element elem) {
         Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
         if (o instanceof Tag) {
            Tag kind = (Tag)o;
            if (kind == Tag.IMG) {
               return new MyImageView(elem, MyHTMLEditorKit.c);
            }
         }

         return super.create(elem);
      }
   }
}
