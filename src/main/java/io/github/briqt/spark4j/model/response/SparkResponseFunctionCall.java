package io.github.briqt.spark4j.model.response;

import java.io.Serializable;

/**
 * SparkResponseFunctionCall
 *
 * @author briqt
 * @date 2023/11/25
 */
public class SparkResponseFunctionCall implements Serializable {
    private static final long serialVersionUID = -1586729944571910329L;

    private String arguments;

    private String name;

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
