import request from '@/utils/request'
import type { PromptVO, PromptCreateRequest, FrameAnalysisVO, AiVideoVO, AiVideoCreateRequest, VideoFrameVO, AgentReportVO } from '@/types/agent'

export const agentApi = {
  // ==================== Prompt Analysis ====================
  analyzeFrame(frameId: number): Promise<FrameAnalysisVO> {
    return request.post(`/agent/frames/${frameId}/analyze`)
  },

  // ==================== Prompt Library ====================
  createPrompt(data: PromptCreateRequest): Promise<PromptVO> {
    return request.post('/agent/prompts', data)
  },
  getPrompt(id: number): Promise<PromptVO> {
    return request.get(`/agent/prompts/${id}`)
  },
  searchPrompts(params?: { keyword?: string; promptType?: string; styleTag?: string; page?: number; size?: number }): Promise<PromptVO[]> {
    return request.get('/agent/prompts', { params })
  },
  recordPromptUse(id: number): Promise<void> {
    return request.post(`/agent/prompts/${id}/use`)
  },
  deletePrompt(id: number): Promise<void> {
    return request.delete(`/agent/prompts/${id}`)
  },

  // ==================== AI Videos ====================
  getAiVideoByAsset(assetId: number): Promise<AiVideoVO> {
    return request.get(`/agent/videos/asset/${assetId}`)
  },
  listAiVideos(projectId?: number): Promise<AiVideoVO[]> {
    return request.get('/agent/videos', { params: projectId ? { projectId } : {} })
  },
  analyzeVision(videoId: number): Promise<{totalFrames:number,analyzedFrames:number}> {
    return request.post(`/agent/videos/${videoId}/analyze-vision`)
  },
  generateNextVersion(videoId: number, projectId: number): Promise<any> {
    return request.post(`/agent/videos/${videoId}/generate-next-version`, null, { params: { projectId } })
  },
  createAiVideo(data: AiVideoCreateRequest): Promise<AiVideoVO> {
    return request.post('/agent/videos', data)
  },
  updateAiVideo(id: number, data: AiVideoCreateRequest): Promise<AiVideoVO> {
    return request.put(`/agent/videos/${id}`, data)
  },
  deleteAiVideo(id: number): Promise<void> {
    return request.delete(`/agent/videos/${id}`)
  },
  getVideoFrames(videoId: number): Promise<VideoFrameVO[]> {
    return request.get(`/agent/videos/${videoId}/frames`)
  },
  getKeyframes(videoId: number): Promise<VideoFrameVO[]> {
    return request.get(`/agent/videos/${videoId}/keyframes`)
  },
  addFrame(videoId: number, frame: { frameNumber?: number; thumbnailUrl?: string; promptText?: string; parameters?: string; isKeyframe?: number; tags?: string }): Promise<VideoFrameVO> {
    return request.post(`/agent/videos/${videoId}/frames`, null, { params: frame })
  },

  // ==================== Data Tasks ====================
  createTask(data: { name: string; platform: string; configJson?: string; cronExpression?: string }): Promise<any> {
    return request.post('/agent/tasks', data)
  },
  listTasks(page?: number, size?: number): Promise<any[]> {
    return request.get('/agent/tasks', { params: { page, size } })
  },
  executeTask(id: number): Promise<void> {
    return request.post(`/agent/tasks/${id}/execute`)
  },
  deleteTask(id: number): Promise<void> {
    return request.delete(`/agent/tasks/${id}`)
  },
  getTaskRecords(taskId: number): Promise<any[]> {
    return request.get(`/agent/records/${taskId}`)
  },

  // ==================== Reports ====================
  generateReport(title: string, type: string, templateId?: number, projectId?: number): Promise<any> {
    return request.post('/agent/reports', null, { params: { title, type, templateId, projectId } })
  },
  listReports(projectId?: number, page?: number, size?: number): Promise<AgentReportVO[]> {
    return request.get('/agent/reports', { params: { projectId, page, size } })
  },
  deleteReport(id: number): Promise<void> {
    return request.delete(`/agent/reports/${id}`)
  },
  listTemplates(): Promise<any[]> {
    return request.get('/agent/templates')
  },
}
