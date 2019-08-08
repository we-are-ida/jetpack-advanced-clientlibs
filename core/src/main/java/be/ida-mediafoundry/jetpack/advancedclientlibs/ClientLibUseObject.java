package be.ida-mediafoundry.jetpack.advancedclientlibs;

import com.adobe.cq.sightly.WCMUsePojo;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.scripting.SlingBindings;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ClientLibUseObject extends WCMUsePojo {

    private static final String BINDINGS_CATEGORIES = "categories";
    private static final String BINDINGS_MODE = "mode";
    private static final String BINDINGS_OPTION = "option";
    private static final String JS = "js";
    private static final String CSS = "css";
    private static final String DEFER = "defer";
    private static final String ASYNC = "async";

    private HtmlLibraryManager htmlLibraryManager = null;
    private String[] categories;
    private String mode;
    private String option;
    private Logger log;

    @Override
    public void activate() throws Exception {
        Object categoriesObject = get(BINDINGS_CATEGORIES, Object.class);
        option = get(BINDINGS_OPTION, String.class);
        log = get(SlingBindings.LOG, Logger.class);

        if (categoriesObject != null) {
            if (categoriesObject instanceof Object[]) {
                Object[] categoriesArray = (Object[]) categoriesObject;
                categories = new String[categoriesArray.length];
                int i = 0;
                for (Object o : categoriesArray) {
                    if (o instanceof String) {
                        categories[i++] = ((String) o).trim();
                    }
                }
            } else if (categoriesObject instanceof String) {
                categories = ((String) categoriesObject).split(",");
                int i = 0;
                for (String c : categories) {
                    categories[i++] = c.trim();
                }
            }
            if (categories != null && categories.length > 0) {
                mode = get(BINDINGS_MODE, String.class);
                htmlLibraryManager = getSlingScriptHelper().getService(HtmlLibraryManager.class);
            }
        }
    }

    //is called from the sightly template
    public String include() {
        StringWriter sw = new StringWriter();
        try {
            if (categories == null || categories.length == 0) {
                log.error("'categories' option might be missing from the invocation of the /apps/granite/sightly/templates/clientlib.html" +
                    "client libraries template library. Please provide a CSV list or an array of categories to include.");
            } else {
                PrintWriter out = new PrintWriter(sw);
                if (JS.equalsIgnoreCase(mode)) {
                    htmlLibraryManager.writeJsInclude(getRequest(), out, categories);
                } else if (CSS.equalsIgnoreCase(mode)) {
                    htmlLibraryManager.writeCssInclude(getRequest(), out, categories);
                } else {
                    htmlLibraryManager.writeIncludes(getRequest(), out, categories);
                }
            }
        } catch (IOException e) {
            log.error("Failed to include client libraries {}", categories);
        }

        String output = sw.toString();

        if (StringUtils.isNotEmpty(output) && StringUtils.isNotBlank(option)) {
            if (option.equals(DEFER)) {
                output = output.replaceAll(".js\"></script>", ".js\" defer></script>");
            } else if (option.equals(ASYNC)) {
                output = output.replaceAll(".js\"></script>", ".js\" async></script>");
            }
        }

        return output.trim();
    }
}
