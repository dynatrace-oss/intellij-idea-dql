package pl.thedeem.intellij.dql.definition.model;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.OptBoolean;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public final class DQLDefinition {
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, DataType> dataTypes;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, ParameterValueType> parameterValueTypes;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, Command> commands;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, FunctionCategory> functionCategories;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, Function> functions;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, Operator> operators;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, Operator> searchOperators;

    public DQLDefinition() {
    }

    public DQLDefinition(
            Map<String, DataType> dataTypes,
            Map<String, ParameterValueType> parameterValueTypes,
            Map<String, Command> commands,
            Map<String, FunctionCategory> functionCategories,
            Map<String, Function> functions,
            Map<String, Operator> operators,
            Map<String, Operator> searchOperators
    ) {
        this.dataTypes = dataTypes;
        this.parameterValueTypes = parameterValueTypes;
        this.commands = commands;
        this.functionCategories = functionCategories;
        this.functions = functions;
        this.operators = operators;
        this.searchOperators = searchOperators;
    }

    public static @NotNull DQLDefinition empty() {
        return new DQLDefinition(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    public Map<String, DataType> dataTypes() {
        return dataTypes;
    }

    public Map<String, ParameterValueType> parameterValueTypes() {
        return parameterValueTypes;
    }

    public Map<String, Command> commands() {
        return commands;
    }

    public Map<String, FunctionCategory> functionCategories() {
        return functionCategories;
    }

    public Map<String, Function> functions() {
        return functions;
    }

    public Map<String, Operator> operators() {
        return operators;
    }

    public Map<String, Operator> searchOperators() {
        return searchOperators;
    }

    public void setDataTypes(Map<String, DataType> dataTypes) {
        this.dataTypes = dataTypes;
    }

    public void setParameterValueTypes(Map<String, ParameterValueType> parameterValueTypes) {
        this.parameterValueTypes = parameterValueTypes;
    }

    public void setCommands(Map<String, Command> commands) {
        this.commands = commands;
    }

    public void setFunctionCategories(Map<String, FunctionCategory> functionCategories) {
        this.functionCategories = functionCategories;
    }

    public void setFunctions(Map<String, Function> functions) {
        this.functions = functions;
    }

    public void setOperators(Map<String, Operator> operators) {
        this.operators = operators;
    }

    public void setSearchOperators(Map<String, Operator> searchOperators) {
        this.searchOperators = searchOperators;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DQLDefinition) obj;
        return Objects.equals(this.dataTypes, that.dataTypes) &&
                Objects.equals(this.parameterValueTypes, that.parameterValueTypes) &&
                Objects.equals(this.commands, that.commands) &&
                Objects.equals(this.functionCategories, that.functionCategories) &&
                Objects.equals(this.functions, that.functions) &&
                Objects.equals(this.operators, that.operators) &&
                Objects.equals(this.searchOperators, that.searchOperators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataTypes, parameterValueTypes, commands, functionCategories, functions, operators, searchOperators);
    }

    @Override
    public String toString() {
        return "DQLDefinition[" +
                "dataTypes=" + dataTypes + ", " +
                "parameterValueTypes=" + parameterValueTypes + ", " +
                "commands=" + commands + ", " +
                "functionCategories=" + functionCategories + ", " +
                "functions=" + functions + ", " +
                "operators=" + operators + ", " +
                "searchOperators=" + searchOperators + ']';
    }

}
