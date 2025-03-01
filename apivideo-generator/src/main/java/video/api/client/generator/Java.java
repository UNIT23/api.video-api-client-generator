package video.api.client.generator;

import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.templating.mustache.IndentedLambda;
import org.openapitools.codegen.templating.mustache.LowercaseLambda;
import org.openapitools.codegen.templating.mustache.TitlecaseLambda;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static video.api.client.generator.Common.populateOperationResponse;

public class Java extends JavaClientCodegen {

    public static final String VENDOR_X_CLIENT_IGNORE = "x-client-ignore";
    public static final List<String> PARAMETERS_TO_HIDE_IN_CLIENT_DOC = Arrays.asList("currentPage", "pageSize");

    public Java() {
        super();
        this.modelTestTemplateFiles.remove("model_test.mustache");
    }

    @Override
    public void postProcessParameter(CodegenParameter parameter) {
        super.postProcessParameter(parameter);
    }


    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
        Common.replaceDescriptionsAndSamples(objs, "java");

        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");

            ops.sort(Common.getCodegenOperationComparator());

            if (ops.stream().allMatch(op -> Boolean.TRUE.equals(op.vendorExtensions.get("x-client-hidden")))) {
                objs.put("x-client-hidden", true);
            }

            for (CodegenOperation operation : ops) {

                if (StringUtils.isNotBlank((String) operation.vendorExtensions.get("x-client-action"))) {
                    operation.operationId = (String) operation.vendorExtensions.get("x-client-action");
                    operation.nickname = operation.operationId;
                } else {
                    throw new RuntimeException("Missing x-client-action value for operation " + operation.operationId);
                }

                operation.vendorExtensions.put("x-client-copy-from-response", operation.allParams.stream()
                        .filter(p -> Boolean.TRUE.equals(p.vendorExtensions.get("x-client-copy-from-response")))
                        .peek(a -> a.vendorExtensions.put("getter", toGetter(a.paramName)))
                        .collect(Collectors.toList()));


                applyToAllParams(operation, (params) -> params.removeIf(pp -> getVendorExtensionBooleanValue(pp, VENDOR_X_CLIENT_IGNORE)));

                applyToAllParams(operation, (params) ->
                        params.stream()
                                .flatMap(p -> p.vars.stream())
                                .filter(p -> PARAMETERS_TO_HIDE_IN_CLIENT_DOC.contains(p.baseName))
                                .forEach(p -> p.vendorExtensions.put("x-client-doc-hidden", true))
                );

                operation.allParams.stream()
                        .flatMap(p -> p.vars.stream())
                        .forEach(v -> {
                            if (v.dataType.equals("List<String>")) {
                                v.example = "Arrays.asList(" + v.example
                                        .replaceAll("\\[", "")
                                        .replaceAll("\\]", "")
                                        .replaceAll("\\\\\"", "\"") + ")";
                            } else if (v.isArray) {
                                v.example = "Collections.<" + v.items.dataType + ">emptyList()";
                            } else if (v.isString) {
                                v.example = "\"" + v.example + "\"";
                            } else if (v.isDateTime) {
                                v.example = "OffsetDateTime.parse(\"" + v.example + "\")";
                            }
                        });

                if (getVendorExtensionBooleanValue(operation, "x-client-paginated")) {
                    handlePagination(allModels, operation);
                }

                operation.allParams.stream().forEach(param -> {
                    switch (param.dataType) {
                        case "URI":
                            param.vendorExtensions.put("testConstructor", "URI.create(\"https://api.video\")");
                        case "File":
                            param.vendorExtensions.put("testConstructor", "new File(\"\")");
                            break;
                        case "String":
                            param.vendorExtensions.put("testConstructor", Optional.ofNullable(param.example).orElse("\"\""));
                            break;
                        case "Integer":
                            param.vendorExtensions.put("testConstructor", "123");
                            break;
                        default:
                            param.vendorExtensions.put("testConstructor", "new " + param.dataType + "()");
                    }
                });

                String folder = getOutputDir() + "/src/test/resources/payloads/" + operation.baseName.toLowerCase() + "/" + operation.vendorExtensions.get("x-client-action") + "/responses/";
                operation.responses.forEach(response -> populateOperationResponse(openAPI, operation, response, additionalProperties, folder));
            }
        }
        return super.postProcessOperationsWithModels(objs, allModels);
    }

    @Override
    public void processOpts() {
        super.processOpts();
        ChangeLog changelog = ChangeLog.parse(additionalProperties);
        additionalProperties.put("artifactVersion", changelog.getLastVersion().getName());
        changelog.writeTo(this.getOutputDir());

        additionalProperties.put("unescape", new UnescapeLambda());
        additionalProperties.put("titlecase", new TitlecaseLambda());
        additionalProperties.put("lower", new LowercaseLambda());
        additionalProperties.put("indented_16", new IndentedLambda(16, " "));

        List<String> skippedFiles = Arrays.asList(
                "ServerConfiguration.mustache",
                "ServerVariable.mustache",
                "GzipRequestInterceptor.mustache",
                "auth/HttpBasicAuth.mustache",
                "auth/HttpBearerAuth.mustache",
                "auth/Authentication.mustache",
                "auth/ApiKeyAuth.mustache",
                "Configuration.mustache");
        supportingFiles.removeIf(e -> skippedFiles.contains(e.getTemplateFile()));
    }



    private void handlePagination(List<Object> allModels, CodegenOperation operation) {
        Optional<Map> map = allModels.stream().filter(m -> ((CodegenModel) ((Map) m).get("model")).classname.equals(operation.returnType)).map(a -> (Map) a).findFirst();
        map.ifPresent(a -> {
            CodegenModel model = (CodegenModel) a.get("model");
            System.out.println(model);
            model.allVars.stream().filter(v -> v.name.equals("data")).findFirst().ifPresent(codegenProperty -> {
                Map<String, String> paginationProperties = new HashMap<>();
                paginationProperties.put("type", codegenProperty.complexType);
                paginationProperties.put("getter", codegenProperty.getter);
                operation.vendorExtensions.put("x-pagination", paginationProperties);
            });
        });
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        Map<String, Object> stringObjectMap = super.postProcessModels(objs);
        ((ArrayList) stringObjectMap.get("imports")).removeIf((v) -> ((Map) v).values().contains("org.openapitools.jackson.nullable.JsonNullable"));
        return stringObjectMap;
    }

    private void applyToAllParams(CodegenOperation operation, Consumer<List<CodegenParameter>> consumer) {
        if (operation.allParams != null) {
            consumer.accept(operation.headerParams);
            consumer.accept(operation.bodyParams);
            consumer.accept(operation.pathParams);
            consumer.accept(operation.formParams);
            consumer.accept(operation.cookieParams);
            consumer.accept(operation.allParams);
        }
    }

    private boolean getVendorExtensionBooleanValue(CodegenParameter parameter, String name) {
        return parameter.vendorExtensions.containsKey(name) && (boolean) parameter.vendorExtensions.get(name);
    }

    private boolean getVendorExtensionBooleanValue(CodegenOperation operation, String name) {
        return operation.vendorExtensions.containsKey(name) && (boolean) operation.vendorExtensions.get(name);
    }

}
