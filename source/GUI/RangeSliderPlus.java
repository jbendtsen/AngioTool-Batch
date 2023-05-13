package GUI;

//import com.jidesoft.plaf.LookAndFeelFactory;
//import com.jidesoft.plaf.UIDefaultsLookup;
//import java.lang.reflect.Method;
import javax.swing.DefaultBoundedRangeModel;
//import javax.swing.JComponent;
import javax.swing.JSlider;
//import javax.swing.UIManager;
//import javax.swing.plaf.ComponentUI;

public class RangeSliderPlus extends JSlider {
   private static final String uiClassID = "SliderUI";
   private boolean _rangeDraggable = true;
   public static final String CLIENT_PROPERTY_MOUSE_POSITION = "RangeSlider.mousePosition";

   public RangeSliderPlus() {
   }

   public RangeSliderPlus(int orientation) {
      super(orientation);
   }

   public RangeSliderPlus(int min, int max) {
      super(min, max);
   }

   public RangeSliderPlus(int min, int max, int low, int high) {
      super(new DefaultBoundedRangeModel(low, high - low, min, max));
   }

   @Override
   public void updateUI() {
      super.updateUI();
      /*
      if (UIDefaultsLookup.get("RangeSliderUI") == null) {
         LookAndFeelFactory.installJideExtension();
      }

      try {
         Class<?> uiClass = Class.forName(UIManager.getString("RangeSliderUI"));
         Class acClass = JComponent.class;
         Method m = uiClass.getMethod("createUI", acClass);
         if (m != null) {
            Object uiObject = m.invoke(null, this);
            this.setUI((ComponentUI)uiObject);
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }
      */
   }

   @Override
   public String getUIClassID() {
      return "SliderUI";
   }

   public int getLowValue() {
      return this.getModel().getValue();
   }

   public int getHighValue() {
      return this.getModel().getValue() + this.getModel().getExtent();
   }

   public boolean contains(int value) {
      return value >= this.getLowValue() && value <= this.getHighValue();
   }

   @Override
   public void setValue(int value) {
      //super.setValue(value);
      /*
      Object clientProperty = this.getClientProperty("RangeSlider.mousePosition");
      if (clientProperty != null) {
         if (Boolean.TRUE.equals(clientProperty)) {
            this.setLowValue(value);
         } else {
            this.setHighValue(value);
         }
      } else {
         this.setLowValue(value);
      }
      */
   }

   public void setLowValue(int lowValue) {
      int high;
      if (lowValue + this.getModel().getExtent() > this.getMaximum()) {
         high = this.getMaximum();
      } else {
         high = this.getHighValue();
      }

      int extent = high - lowValue;
      this.getModel().setRangeProperties(lowValue, extent, this.getMinimum(), this.getMaximum(), true);
   }

   public void setHighValue(int highValue) {
      this.getModel().setExtent(highValue - this.getLowValue());
   }

   public boolean isRangeDraggable() {
      return this._rangeDraggable;
   }

   public void setRangeDraggable(boolean rangeDraggable) {
      this._rangeDraggable = rangeDraggable;
   }
}
