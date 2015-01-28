package de.deverado.framework.core.problemreporting;

public class Problem {

    private String ref;

    private String message;

    public Problem() {
    }

    public Problem(String ref) {
        ref(ref);
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Problem ref(String ref) {
        setRef(ref);
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Problem message(String message) {
        setMessage(message);
        return this;
    }

    @Override
    public String toString() {
        return "" + getRef() + "[" + getMessage() + "]";
    }
}
