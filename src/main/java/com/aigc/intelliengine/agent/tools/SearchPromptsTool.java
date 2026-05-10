package com.aigc.intelliengine.agent.tools;

import com.aigc.intelliengine.agent.PromptLibraryService;
import com.aigc.intelliengine.agent.model.vo.SemanticPromptVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchPromptsTool implements AgentTool {

    private final PromptLibraryService promptLibraryService;

    @Override public String name() { return "search_prompts"; }

    @Override
    public String description() {
        return "语义搜索Prompt库，找到与查询语义相似的Prompt。使用RAG向量检索，输入自然语言查询，返回匹配的Prompt列表。";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "query", Map.of("type", "string", "description", "自然语言搜索查询"),
                "topK", Map.of("type", "integer", "description", "返回结果数，默认5")
            ),
            "required", java.util.List.of("query")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String query = (String) args.get("query");
        int topK = args.containsKey("topK") ? ((Number) args.get("topK")).intValue() : 5;
        List<SemanticPromptVO> results = promptLibraryService.semanticSearch(query, topK);
        if (results.isEmpty()) return "未找到与 '" + query + "' 相似的Prompt";
        return results.stream()
                .map(p -> String.format("[#%d score=%.2f] %s", p.getId(), p.getSimilarityScore(),
                        p.getPromptText().length() > 60 ? p.getPromptText().substring(0, 60) + "..." : p.getPromptText()))
                .collect(Collectors.joining("\n  "));
    }
}
