package io.github.thecsdev.tcdcommons.api.client.gui.widget;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProviderSetter;
import net.minecraft.text.Text;

public @Virtual class TButtonWidget extends TClickableWidget implements ITextProviderSetter
{
	// ==================================================
	protected @Nullable Text text;
	protected @Nullable Consumer<TButtonWidget> onClick;
	protected @Nullable UITexture icon;
	// ==================================================
	public TButtonWidget(int x, int y, int width, int height) { this(x, y, width, height, null); }
	public TButtonWidget(int x, int y, int width, int height, Text text) { this(x, y, width, height, text, null); }
	public TButtonWidget(int x, int y, int width, int height, Text text, Consumer<TButtonWidget> onClick)
	{
		super(x, y, Math.max(width, 5), Math.max(height, 5));
		this.text = text;
		this.onClick = onClick;
	}
	// --------------------------------------------------
	public final @Nullable @Override Text getText() { return this.text; }
	public @Virtual @Override void setText(@Nullable Text text) { this.text = text; }
	// --------------------------------------------------
	public final @Nullable Consumer<TButtonWidget> getOnClick() { return this.onClick; }
	public @Virtual void setOnClick(@Nullable Consumer<TButtonWidget> onClick) { this.onClick = onClick; }
	// --------------------------------------------------
	public final @Nullable UITexture getIcon() { return this.icon; }
	public @Virtual void setIcon(@Nullable UITexture icon) { this.icon = icon; }
	// ==================================================
	protected @Virtual @Override void onClick()
	{
		if(this.onClick == null) return;
		this.onClick.accept(this);
	}
	// --------------------------------------------------
	public @Virtual @Override void render(TDrawContext pencil)
	{
		pencil.drawTButton(getButtonTextureY());
		renderBackground(pencil);
		pencil.enableScissor(getX(), getY(), getEndX(), getEndY());
		pencil.drawTElementTextTH(this.text, HorizontalAlignment.CENTER);
		pencil.disableScissor();
	}
	protected @Virtual void renderBackground(TDrawContext pencil)
	{
		if(this.icon != null)
			this.icon.drawTexture(pencil, getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);
	}
	// ==================================================
}