package com.aigc.intelliengine.agent.tools;

import java.util.Map;

/**
 * Agent 工具契约 — 每个工具是一个可被Agent调用的原子能力.
 * 设计对应 MCP协议的概念: name + description + inputSchema + execute.
 */
public interface AgentTool {

    /** 工具名称，如 "analyze_prompt" */
    String name();

    /** 工具描述，告诉LLM这个工具做什么、何时使用 */
    String description();

    /** JSON Schema风格的参数定义 */
    Map<String, Object> inputSchema();

    /** 执行工具，返回结果字符串 */
    String execute(Map<String, Object> args);
}
