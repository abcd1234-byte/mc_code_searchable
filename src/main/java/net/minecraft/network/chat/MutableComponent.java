package net.minecraft.network.chat;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;

public interface MutableComponent extends Component {
   MutableComponent setStyle(Style pStyle);

   default MutableComponent append(String pString) {
      return this.append(new TextComponent(pString));
   }

   MutableComponent append(Component pSibling);

   default MutableComponent withStyle(UnaryOperator<Style> pModifyFunc) {
      this.setStyle(pModifyFunc.apply(this.getStyle()));
      return this;
   }

   default MutableComponent withStyle(Style pStyle) {
      this.setStyle(pStyle.applyTo(this.getStyle()));
      return this;
   }

   default MutableComponent withStyle(ChatFormatting... pFormats) {
      this.setStyle(this.getStyle().applyFormats(pFormats));
      return this;
   }

   default MutableComponent withStyle(ChatFormatting pFormat) {
      this.setStyle(this.getStyle().applyFormat(pFormat));
      return this;
   }
}