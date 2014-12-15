package io.vertx.componentdiscovery.utils

class DoWhile {
	private Closure block

	static DoWhile repeat(Closure block) {
		new DoWhile(block:block)
	}

	void until(Closure test){
		block()
		while(!test()) {
			block()
		}
	}
}
