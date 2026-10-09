package framework.web;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private String url;
    private Map<String, Object> attributes = new HashMap<>();

    public ModelAndView(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}
