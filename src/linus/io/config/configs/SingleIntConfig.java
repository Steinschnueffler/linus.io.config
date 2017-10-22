package linus.io.config.configs;

import linus.io.config.Config;
import linus.io.config.SingleConfig;

public class SingleIntConfig extends SingleConfig<Integer>{
	public SingleIntConfig() {}

	private int value = 0;

	public SingleIntConfig(String name, int value) {
		super(name);
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	@Override
	public Config<Integer> read(String[] lines) {
		name = lines[0].substring(0, lines[0].indexOf(SEPARATOR)).trim();
		value = Integer.parseInt(lines[0].substring(lines[0].indexOf(SEPARATOR) + 1, lines[0].length()).trim());
		return this;
	}

	public int getPrimitiveValue(){
		return value;
	}

	@Override
	protected void setValue(Integer value) {
		this.value = value;
	}

}
