package top.aurora.lordofmysteries.knowledge;

public final class KnowledgeText {

    private KnowledgeText() {}

    public static String translationKey(String id) {
        if (id == null || id.isBlank()) return "knowledge.lord_of_mysteries.unknown";
        int separator = id.indexOf(':');
        String namespace = separator >= 0 ? id.substring(0, separator) : "lord_of_mysteries";
        String path = separator >= 0 ? id.substring(separator + 1) : id;
        if (path.startsWith("knowledge/")) path = path.substring("knowledge/".length());
        return "knowledge." + namespace + "." + path.replace('/', '.');
    }

    public static String pathwayTranslationKey(String id) {
        if (id == null || id.isBlank()) return "pathway.lord_of_mysteries.commoner";
        int separator = id.indexOf(':');
        String namespace = separator >= 0 ? id.substring(0, separator) : "lord_of_mysteries";
        String path = separator >= 0 ? id.substring(separator + 1) : id;
        return "pathway." + namespace + "." + path.replace('/', '.');
    }
}
