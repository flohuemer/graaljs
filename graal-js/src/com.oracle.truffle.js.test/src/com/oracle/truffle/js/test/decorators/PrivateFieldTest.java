package com.oracle.truffle.js.test.decorators;

import org.junit.Test;

public class PrivateFieldTest extends DecoratorTest{
    @Test
    public void readWriteonlyAccessor() {
        String source = "class C {" +
                "#message;" +
                "set #test(value) {" +
                "   this.#message = value;" +
                "}" +
                "b(){" +
                "   let a = this.#test;" +
                "}" +
                "}" +
                "let c = new C();" +
                "c.b();";
        testError(source, "Accessor #test has no getter.");
    }

    @Test
    public void writeReadonlyField() {
        String source = "" +
                "function readonly(d) {" +
                "d.writeable = false;" +
                "return d;" +
                "}" +
                "class C {" +
                "@readonly" +
                "#message;" +
                "set test(value) {" +
                "   this.#message = value;" +
                "}" +
                "}" +
                "let c = new C();" +
                "c.test = 'test';";
        testError(source, "Field #message is not writable.");
    }
}
