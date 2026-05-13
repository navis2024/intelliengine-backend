<template>
  <div class="h-full flex flex-col">
    <!-- Top Toolbar -->
    <header class="h-12 shrink-0 flex items-center justify-between px-6 border-b border-border bg-card/50">
      <div class="flex items-center gap-4">
        <router-link :to="`/projects/${projectId}`" class="text-xs text-muted-foreground hover:text-foreground transition-colors flex items-center gap-1">
          <ArrowLeft class="w-3.5 h-3.5" /> {{ projectName }}
        </router-link>
        <span class="text-muted-foreground">/</span>
        <span class="text-sm font-medium">Draft Board</span>
        <span class="text-[10px] text-muted-foreground bg-muted px-2 py-0.5 rounded-full">v{{ currentVersion }}</span>
      </div>
      <div class="flex items-center gap-3">
        <button class="btn-ghost text-xs h-7 gap-1.5" @click="undoShape"><Undo2 class="w-3 h-3" /> Undo</button>
        <button class="btn-ghost text-xs h-7 gap-1.5" @click="clearShapes"><Trash2 class="w-3 h-3" /> Clear</button>
        <button class="btn-primary text-xs h-7 gap-1.5" @click="generateNextVersion" :disabled="generating">
          <span v-if="generating" class="inline-block w-3 h-3 border-2 border-background border-t-transparent rounded-full animate-spin" />
          {{ generating ? 'Generating...' : 'Generate Next Version' }}
        </button>
      </div>
    </header>

    <!-- Three-Column Workspace -->
    <div class="flex-1 flex overflow-hidden">
      <!-- Left: Frame Strip -->
      <aside class="w-48 shrink-0 border-r border-border bg-card/30 flex flex-col">
        <div class="h-10 flex items-center px-3 border-b border-border">
          <span class="text-[10px] font-medium uppercase tracking-widest text-muted-foreground">Frames</span>
          <span class="ml-auto text-[10px] text-muted-foreground">{{ frameList.length }} total</span>
        </div>
        <div class="flex-1 overflow-y-auto p-2 space-y-1">
          <div v-for="f in frameList" :key="f.id"
            @click="selectFrame(f)"
            class="group relative rounded cursor-pointer border transition-all"
            :class="activeFrame?.id === f.id ? 'border-blue-500/50 bg-blue-500/5' : 'border-transparent hover:border-border'">
            <div class="aspect-video bg-muted flex items-center justify-center text-center overflow-hidden">
              <img v-if="f.thumbnailUrl" :src="f.thumbnailUrl" class="w-full h-full object-cover opacity-60 group-hover:opacity-80 transition-opacity" />
              <span v-else class="text-[8px] text-muted-foreground">Frame #{{ f.frameNumber }}</span>
            </div>
            <div class="px-2 py-1.5 flex items-center justify-between">
              <span class="text-[10px] font-mono text-muted-foreground">{{ formatTimestamp(f.timestamp) }}</span>
              <span v-if="f.isKeyframe" class="w-1.5 h-1.5 rounded-full bg-yellow-400" />
              <span v-if="getFrameAnnotations(f.id).length" class="text-[9px] text-blue-400">{{ getFrameAnnotations(f.id).length }}</span>
            </div>
          </div>
        </div>
      </aside>

      <!-- Center: Canvas Area -->
      <main class="flex-1 flex flex-col bg-black">
        <!-- Drawing Toolbar -->
        <div class="h-9 shrink-0 flex items-center gap-1 px-4 border-b border-border bg-card/30">
          <button v-for="tool in tools" :key="tool.id" @click="activeTool=tool.id"
            class="w-7 h-7 rounded flex items-center justify-center transition-colors text-xs"
            :class="activeTool===tool.id ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30' : 'text-muted-foreground hover:text-foreground hover:bg-muted'"
            :title="tool.label">
            <component :is="tool.icon" class="w-3.5 h-3.5" />
          </button>
          <div class="w-px h-4 bg-border mx-1" />
          <input type="color" v-model="drawColor" class="w-6 h-6 rounded cursor-pointer border-0 bg-transparent" title="Color" />
          <span class="text-[10px] text-muted-foreground ml-2">Draw on canvas to annotate visual regions</span>
        </div>
        <!-- Video Player + Drawing Canvas -->
        <div class="flex-1 relative overflow-hidden bg-black" ref="canvasContainer">
          <div class="absolute inset-0 flex items-center justify-center">
            <video v-if="playUrl" ref="videoPlayer" :src="playUrl" class="max-w-full max-h-full object-contain"
              @timeupdate="onVideoTimeUpdate" @loadedmetadata="onVideoLoaded" controls preload="auto" />
            <div v-else-if="activeFrame?.thumbnailUrl" class="relative w-full h-full">
              <img :src="activeFrame.thumbnailUrl" class="w-full h-full object-contain opacity-70" />
            </div>
            <div v-else class="text-center space-y-2">
              <Play class="w-12 h-12 text-foreground/20 mx-auto" />
              <p class="text-xs text-muted-foreground">Frame #{{ activeFrame?.frameNumber || 1 }}</p>
            </div>
          </div>
          <svg ref="svgEl" class="absolute inset-0 w-full h-full pointer-events-none"
            @mousedown="startDraw" @mousemove="onDraw" @mouseup="endDraw" @mouseleave="endDraw">
            <rect v-for="(s, i) in shapes" :key="i"
              :x="s.x" :y="s.y" :width="s.w" :height="s.h"
              :stroke="s.color" :fill="s.type==='rect'?'none':s.color+'33'"
              stroke-width="2" rx="2" />
            <line v-for="(s, i) in shapes.filter(x=>x.type==='arrow')" :key="'a'+i"
              :x1="s.x" :y1="s.y" :x2="s.x+s.w" :y2="s.y+s.h"
              :stroke="s.color" stroke-width="2" />
          </svg>
        </div>
        <!-- Timeline -->
        <div class="h-10 border-t border-border flex items-center px-4 gap-0.5">
          <div v-for="f in frameList" :key="f.id"
            @click="selectFrame(f)"
            class="h-6 rounded-sm cursor-pointer transition-colors"
            :class="activeFrame?.id === f.id ? 'bg-blue-500/40' : 'bg-muted hover:bg-muted-foreground/20'"
            :style="{ width: (100 / frameList.length) + '%' }" />
        </div>
      </main>

      <!-- Right: Properties Panel -->
      <aside class="w-80 shrink-0 border-l border-border bg-card/30 flex flex-col overflow-y-auto">
        <!-- Frame Prompt -->
        <div class="p-4 space-y-4 border-b border-border">
          <h3 class="text-[10px] font-medium uppercase tracking-widest text-muted-foreground">Frame Prompt</h3>
          <div class="space-y-3">
            <div>
              <label class="text-[10px] text-muted-foreground block mb-1.5">Positive Prompt</label>
              <textarea v-model="editingPrompt" class="w-full bg-muted/50 border border-border rounded p-3 text-xs font-mono text-foreground/80 leading-relaxed resize-none focus:outline-none min-h-[80px]" />
            </div>
            <div class="grid grid-cols-2 gap-2 text-[10px]">
              <div><span class="text-muted-foreground">Tool</span><p class="mt-0.5 font-mono">{{ agentTool }}</p></div>
              <div><span class="text-muted-foreground">FPS</span><p class="mt-0.5 font-mono">{{ fps }}</p></div>
            </div>
            <button @click="analyzeAllFrames" class="btn-ghost text-xs w-full gap-1" :disabled="analyzing">
              <Bot class="w-3 h-3" />
              {{ analyzing ? `Analyzing... ${analyzedCount}/${frameList.length}` : (frameList[0]?.promptText ? 'Re-analyze All Frames' : 'Analyze with Agent') }}
            </button>
            <!-- Vision analysis progress -->
            <div v-if="analyzing" class="text-[10px] text-blue-400 text-center">
              Kimi K2.5 is analyzing each frame with vision AI — this may take a minute...
            </div>
          </div>
        </div>

        <!-- Agent Advice -->
        <div v-if="parsedAdvice" class="p-4 border-b border-border space-y-2 bg-blue-500/5">
          <h3 class="text-xs font-semibold text-blue-400 flex items-center gap-1"><Bot class="w-3 h-3" /> Agent Advice</h3>
          <p class="text-xs text-foreground/80 leading-relaxed">{{ parsedAdvice.analysis }}</p>
          <div v-if="parsedAdvice.suggestions?.length" class="space-y-1">
            <p class="text-[10px] font-medium text-muted-foreground">Suggestions:</p>
            <ul class="list-disc pl-4 space-y-0.5">
              <li v-for="(s,i) in parsedAdvice.suggestions" :key="i" class="text-[11px] text-foreground/70">{{ s }}</li>
            </ul>
          </div>
          <div v-if="parsedAdvice.confidence" class="text-[10px] text-muted-foreground">Confidence: {{ (parsedAdvice.confidence*100).toFixed(0) }}%</div>
        </div>

        <!-- Review Comments -->
        <div v-if="reviewAssetId" class="p-4 border-b border-border">
          <ReviewPanel :asset-id="reviewAssetId" :project-id="Number(projectId)" :key="reviewAssetId" />
        </div>

        <!-- Annotations (per-frame) -->
        <div class="p-4 space-y-3 border-b border-border">
          <div class="flex items-center justify-between">
            <h3 class="text-[10px] font-medium uppercase tracking-widest text-muted-foreground">
              Annotations <span v-if="activeFrame" class="text-blue-400 ml-1">· Frame #{{ activeFrame.frameNumber }}</span>
            </h3>
            <button @click="addAnnotation" class="text-[10px] text-blue-400 hover:text-blue-300 transition-colors">+ Add</button>
          </div>
          <div v-if="activeFrameAnnotations.length === 0" class="text-xs text-muted-foreground text-center py-4">
            No annotations for this frame yet
          </div>
          <div class="space-y-2">
            <div v-for="a in activeFrameAnnotations" :key="a.id"
              class="border border-border rounded p-3 space-y-1.5 hover:border-foreground/10 transition-colors">
              <div class="flex items-start justify-between">
                <span class="text-[10px] font-medium">{{ a.author }}</span>
                <span class="text-[9px] text-muted-foreground font-mono">{{ a.timestamp >= 0 ? formatTimestamp(a.timestamp) : '' }}</span>
              </div>
              <p class="text-xs text-foreground/70 leading-relaxed">{{ a.content }}</p>
              <p v-if="a.suggestion" class="text-[10px] text-blue-400 bg-blue-500/5 p-1.5 rounded mt-1">💡 {{ a.suggestion }}</p>
              <button @click="removeAnnotation(a.id)" class="text-[9px] text-destructive mt-1">Remove</button>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- Add Annotation Dialog -->
    <div v-if="showAddAnnotation" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60" @click.self="showAddAnnotation=false">
      <div class="w-96 rounded-lg border border-border bg-card p-5 shadow-xl space-y-3">
        <h3 class="text-sm font-semibold">Add Annotation — Frame #{{ activeFrame?.frameNumber }}</h3>
        <textarea v-model="newAnnotationText" class="input-field min-h-[80px]" placeholder="Your annotation..." />
        <input v-model="newAnnotationSuggestion" class="input-field" placeholder="Suggested prompt change (optional)" />
        <div class="flex justify-end gap-2 pt-1">
          <button @click="showAddAnnotation=false" class="btn-ghost text-xs">Cancel</button>
          <button @click="submitAnnotation" class="btn-primary text-xs">Submit</button>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div v-if="toast" class="fixed bottom-8 left-1/2 -translate-x-1/2 z-50 px-4 py-2 rounded-lg bg-foreground text-background text-sm font-medium shadow-xl animate-fade-in">
      {{ toast }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, Play, Square, ArrowRight, Type, Undo2, Trash2, Bot } from 'lucide-vue-next'
import { agentApi } from '@/api/agent'
import { assetApi } from '@/api/asset'
import ReviewPanel from '@/components/ReviewPanel.vue'
import type { FrameAnalysisVO } from '@/types/agent'

const route = useRoute()
const projectId = route.params.id as string
const projectName = ref('AI Short Drama')
const currentVersion = ref(Number(route.query.version) || 3)
const agentTool = ref('Runway Gen-3')
const fps = ref(24)
const editingPrompt = ref('cinematic wide shot, cyberpunk tea house interior, neon lights reflecting off polished wood, volumetric fog, shallow depth of field, 8K, photorealistic')
const activeFrame = ref<any>(null)
const showAddAnnotation = ref(false)
const newAnnotationText = ref('')
const newAnnotationSuggestion = ref('')
const analyzing = ref(false)
const analyzedCount = ref(0)
const generating = ref(false)
const toast = ref('')
const aiVideo = ref<any>(null)
const playUrl = ref('')
const videoPlayer = ref<HTMLVideoElement | null>(null)
const agentAdvice = ref<any>(null)
const reviewAssetId = computed(() => aiVideo.value?.assetId || null)

const parsedAdvice = computed(() => {
  if (!agentAdvice.value) return null
  try { return typeof agentAdvice.value === 'string' ? JSON.parse(agentAdvice.value) : agentAdvice.value }
  catch { return null }
})

// Annotation storage: keyed by frame ID
const frameAnnotations = ref<Record<number, any[]>>({})

const activeFrameAnnotations = computed(() => {
  if (!activeFrame.value) return []
  return frameAnnotations.value[activeFrame.value.id] || []
})

const getFrameAnnotations = (frameId: number) => frameAnnotations.value[frameId] || []

const tools = [
  { id: 'rect', label: 'Rectangle', icon: Square },
  { id: 'arrow', label: 'Arrow', icon: ArrowRight },
  { id: 'text', label: 'Text', icon: Type },
]
const activeTool = ref('rect')
const drawColor = ref('#3b82b6')
const shapes = ref<{ type: string; x: number; y: number; w: number; h: number; color: string }[]>([])
const svgEl = ref<SVGSVGElement | null>(null)
const canvasContainer = ref<HTMLElement | null>(null)
let drawing = false, startX = 0, startY = 0

const frameList = ref<any[]>([])

const formatTimestamp = (t: number) => {
  if (t === undefined || t === null) return '00:00.0'
  const m = Math.floor(t / 60); const s = (t % 60).toFixed(1)
  return `${m.toString().padStart(2, '0')}:${s.padStart(4, '0')}`
}

const selectFrame = (f: any) => {
  activeFrame.value = f
  shapes.value = []
  analysisResult.value = null
  if (f.promptText) editingPrompt.value = f.promptText
  if (videoPlayer.value && f.timestamp != null) {
    videoPlayer.value.currentTime = Number(f.timestamp)
  }
}

const undoShape = () => shapes.value.pop()
const clearShapes = () => { shapes.value = [] }

const getSVGPos = (e: MouseEvent) => {
  const rect = svgEl.value?.getBoundingClientRect()
  if (!rect) return { x: 0, y: 0 }
  return { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

const startDraw = (e: MouseEvent) => { drawing = true; const pos = getSVGPos(e); startX = pos.x; startY = pos.y }
const onDraw = (e: MouseEvent) => {
  if (!drawing) return
  const pos = getSVGPos(e)
  const last = shapes.value[shapes.value.length - 1]
  if (last && last.x === startX && last.y === startY) { last.w = pos.x - startX; last.h = pos.y - startY }
}
const endDraw = (e: MouseEvent) => {
  if (!drawing) return
  drawing = false
  const pos = getSVGPos(e)
  const w = pos.x - startX; const h = pos.y - startY
  if (Math.abs(w) > 3 && Math.abs(h) > 3) {
    shapes.value.push({ type: activeTool.value, x: startX, y: startY, w, h, color: drawColor.value })
  }
}

// ==================== Annotations (per-frame) ====================
const addAnnotation = () => { showAddAnnotation.value = true; newAnnotationText.value = ''; newAnnotationSuggestion.value = '' }
const submitAnnotation = () => {
  if (!newAnnotationText.value.trim() || !activeFrame.value) return
  const fid = activeFrame.value.id
  if (!frameAnnotations.value[fid]) frameAnnotations.value[fid] = []
  frameAnnotations.value[fid].push({
    id: Date.now(), author: 'Me', timestamp: activeFrame.value?.timestamp ?? 0,
    content: newAnnotationText.value, suggestion: newAnnotationSuggestion.value || ''
  })
  showAddAnnotation.value = false
}
const removeAnnotation = (id: number) => {
  if (!activeFrame.value) return
  const fid = activeFrame.value.id
  if (frameAnnotations.value[fid]) {
    frameAnnotations.value[fid] = frameAnnotations.value[fid].filter((a: any) => a.id !== id)
  }
}

// ==================== Agent Integration ====================
const analyzeAllFrames = async () => {
  if (!aiVideo.value?.id || analyzing.value) return
  analyzing.value = true
  analyzedCount.value = 0
  try {
    const result = await agentApi.analyzeVision(Number(aiVideo.value.id))
    analyzedCount.value = result.analyzedFrames || 0
    toast.value = 'Vision analysis done: ' + result.analyzedFrames + '/' + result.totalFrames + ' frames'
    setTimeout(() => toast.value = '', 4000)
    await loadAiVideoData()
  } catch (e) {
    toast.value = 'Vision analysis failed: ' + (e?.message || '')
    setTimeout(() => toast.value = '', 4000)
  } finally { analyzing.value = false }
}

const generateNextVersion = async () => {
  if (!aiVideo.value?.id || generating.value) return
  generating.value = true
  try {
    const result = await agentApi.generateNextVersion(Number(aiVideo.value.id), Number(projectId))
    currentVersion.value = result.version || (currentVersion.value + 1)
    if (result.newPrompt) editingPrompt.value = result.newPrompt
    if (result.agentAdvice) agentAdvice.value = result.agentAdvice
    toast.value = `Version ${result.version || currentVersion.value + 1} generated`
  } catch (e: any) {
    toast.value = 'Generation failed: ' + (e?.message || 'Unknown error')
    setTimeout(() => toast.value = '', 4000)
  } finally { generating.value = false }
}

// 加载项目关联的 AI 视频和帧
const loadAiVideoData = async () => {
  try {
    const videos = await agentApi.listAiVideos(Number(projectId))
    if (!videos?.length) return
    aiVideo.value = videos[0]
    if (aiVideo.value.fps) fps.value = aiVideo.value.fps
    if (aiVideo.value.toolType) agentTool.value = aiVideo.value.toolType
    if (aiVideo.value.promptText) editingPrompt.value = aiVideo.value.promptText
    const frames = await agentApi.getVideoFrames(Number(aiVideo.value.id))
    frameList.value = (frames || []).map((f, idx) => ({
      id: idx + 1,
      realId: Number(f.id),
      frameNumber: f.frameNumber,
      timestamp: f.timestamp,
      isKeyframe: f.isKeyframe,
      thumbnailUrl: '/api/v1/agent/frames/' + f.id + '/thumbnail',
      promptText: f.promptText,
    }))
    if (frameList.value.length) activeFrame.value = frameList.value[0]
    if (aiVideo.value?.assetId) {
      try {
        const url = await assetApi.getPlayUrl(String(aiVideo.value.assetId))
        playUrl.value = (url as any)?.data || url
      } catch { /* video playback unavailable */ }
    }
  } catch (e: any) {
    // 加载失败保持空状态
  }
}

const onVideoTimeUpdate = () => {
  const vp = videoPlayer.value
  if (!vp || !frameList.value.length) return
  const t = vp.currentTime
  let best = frameList.value[0]
  for (const f of frameList.value) {
    if ((Number(f.timestamp) || 0) <= t + 0.5) best = f
  }
  if (activeFrame.value?.id !== best.id) {
    activeFrame.value = best
    if (best.promptText) editingPrompt.value = best.promptText
  }
}
const onVideoLoaded = () => {}
loadAiVideoData()
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(4px); } to { opacity: 1; transform: translateY(0); } }
@keyframes spin { to { transform: rotate(360deg); } }
.animate-spin { animation: spin 0.8s linear infinite; }
</style>
