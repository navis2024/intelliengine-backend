<template>
  <div class="p-8 space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold tracking-tight">Agent Studio</h2>
        <p class="text-sm text-muted-foreground mt-1">RAG语义检索 · ReAct推理链 · Multi-Agent协同</p>
      </div>
      <span class="text-xs text-muted-foreground flex items-center gap-1.5">
        <span class="w-2 h-2 rounded-full bg-green-400" /> kimi-k2.6
      </span>
    </div>

    <div class="flex border-b border-border">
      <button v-for="t in tabs" :key="t.id" @click="activeTab=t.id"
        class="px-5 py-2.5 text-sm font-medium border-b-2 transition-colors -mb-px"
        :class="activeTab===t.id ? 'border-blue-500 text-blue-400' : 'border-transparent text-muted-foreground hover:text-foreground'">
        {{ t.label }}
      </button>
    </div>

    <!-- ═══ RAG ═══ -->
    <div v-if="activeTab==='rag'" class="space-y-5">
      <div class="flex gap-3">
        <input v-model="ragQ" @keyup.enter="doRag" placeholder="自然语言查询，如「赛博朋克城市夜景」..."
          class="flex-1 h-10 px-4 rounded-lg border border-border bg-muted text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-blue-500/50" />
        <button @click="doRag" :disabled="ragBusy" class="bg-foreground text-background h-10 px-5 rounded-lg text-sm font-medium hover:opacity-80 transition-opacity">Search</button>
        <button @click="doReindex" class="border border-border h-10 px-3 rounded-lg text-xs text-muted-foreground hover:text-foreground transition-colors">Rebuild Index</button>
      </div>

      <div v-if="ragBusy" class="text-sm text-muted-foreground text-center py-10">检索中...</div>

      <div v-if="ragList.length" class="space-y-2">
        <div class="text-xs text-muted-foreground">{{ ragList.length }} results</div>
        <div v-for="(r,i) in ragList" :key="i"
          class="rounded-lg border border-border p-4 hover:border-blue-500/20 transition-colors">
          <div class="flex justify-between mb-1.5">
            <span class="text-[11px] font-mono text-blue-400">#{{ i+1 }}</span>
            <span class="text-[10px] text-muted-foreground">{{ r.promptType }}</span>
          </div>
          <p class="text-sm leading-relaxed text-foreground/80">{{ r.promptText }}</p>
          <div v-if="r.styleTags" class="flex flex-wrap gap-1 mt-2">
            <span v-for="t in parseTags(r.styleTags)" :key="t" class="text-[9px] px-1.5 py-0.5 rounded bg-muted text-muted-foreground">{{ t }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══ ReAct ═══ -->
    <div v-if="activeTab==='react'" class="space-y-4">
      <div class="flex gap-3">
        <input v-model="reactTask" @keyup.enter="doReAct" placeholder="输入任务，如「分析项目中的所有视频帧并给出优化建议」..."
          class="flex-1 h-10 px-4 rounded-lg border border-border bg-muted text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-blue-500/50" />
        <button @click="doReAct" :disabled="reactBusy" class="bg-foreground text-background h-10 px-5 rounded-lg text-sm font-medium hover:opacity-80 transition-opacity">
          {{ reactBusy ? 'Thinking...' : 'Execute' }}
        </button>
      </div>

      <div class="flex gap-2 flex-wrap">
        <span v-for="t in toolList" :key="t.name" class="text-[10px] px-2 py-0.5 rounded bg-muted font-mono text-muted-foreground">{{ t.name }}</span>
      </div>

      <div v-if="reactTrace.length" class="rounded-lg border border-border bg-card/50 overflow-hidden">
        <div class="h-8 flex items-center px-4 border-b border-border bg-muted/30 text-xs font-medium">ReAct Trace</div>
        <div class="p-4 space-y-2 max-h-80 overflow-y-auto font-mono text-xs">
          <div v-for="(s,i) in reactTrace" :key="i" class="flex gap-3">
            <span class="shrink-0 w-24 text-right" :class="s.type==='thought'?'text-yellow-400':s.type==='action'?'text-blue-400':s.type==='observation'?'text-green-400':'text-muted-foreground'">{{ s.type?.toUpperCase() }}</span>
            <span class="text-foreground/80 break-all">{{ s.content }}</span>
          </div>
          <div v-if="reactBusy" class="flex items-center gap-2 text-blue-400 pt-1">
            <span class="inline-block w-3 h-3 border-2 border-blue-400 border-t-transparent rounded-full animate-spin" /> Processing...
          </div>
        </div>
      </div>
    </div>

    <!-- ═══ Multi-Agent ═══ -->
    <div v-if="activeTab==='multi'" class="space-y-4">
      <div class="flex gap-3">
        <input v-model="multiTask" @keyup.enter="doMulti" placeholder="输入复杂任务，如「为南天门计划生成完整的优化方案和报告」..."
          class="flex-1 h-10 px-4 rounded-lg border border-border bg-muted text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-blue-500/50" />
        <button @click="doMulti" :disabled="multiBusy" class="bg-foreground text-background h-10 px-5 rounded-lg text-sm font-medium hover:opacity-80 transition-opacity">
          {{ multiBusy ? 'Collaborating...' : 'Launch' }}
        </button>
      </div>

      <div class="grid grid-cols-3 gap-3 text-xs">
        <div class="rounded-lg border border-yellow-500/20 bg-yellow-500/5 p-3"><span class="text-yellow-400 font-semibold">Supervisor</span><p class="text-muted-foreground mt-0.5">任务分解 · Worker分配</p></div>
        <div class="rounded-lg border border-blue-500/20 bg-blue-500/5 p-3"><span class="text-blue-400 font-semibold">Workers ×3</span><p class="text-muted-foreground mt-0.5">并行执行 · 工具调用</p></div>
        <div class="rounded-lg border border-green-500/20 bg-green-500/5 p-3"><span class="text-green-400 font-semibold">Auditor</span><p class="text-muted-foreground mt-0.5">质量审查 · 汇总输出</p></div>
      </div>

      <div v-if="multiTrace.length" class="rounded-lg border border-border bg-card/50 overflow-hidden">
        <div class="h-8 flex items-center px-4 border-b border-border bg-muted/30 text-xs font-medium">Workflow Trace</div>
        <div class="p-4 space-y-2 max-h-80 overflow-y-auto font-mono text-xs">
          <div v-for="(s,i) in multiTrace" :key="i" class="flex gap-3">
            <span class="shrink-0 w-24 text-right" :class="s.agent==='SUPERVISOR'?'text-yellow-400':s.agent==='AUDITOR'?'text-green-400':'text-blue-400'">{{ s.agent }}</span>
            <span class="text-foreground/80 break-all">{{ s.content }}</span>
          </div>
          <div v-if="multiBusy" class="flex items-center gap-2 text-blue-400 pt-1">
            <span class="inline-block w-3 h-3 border-2 border-blue-400 border-t-transparent rounded-full animate-spin" /> Collaborating...
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { agentApi } from '@/api/agent'

const tabs = [{id:'rag',label:'RAG Search'},{id:'react',label:'ReAct Agent'},{id:'multi',label:'Multi-Agent'}]
const activeTab = ref('rag')
const toolList = ref<any[]>([])

// ── RAG ──
const ragQ = ref(''); const ragBusy = ref(false); const ragList = ref<any[]>([])
async function doRag() { if(!ragQ.value||ragBusy.value)return; ragBusy.value=true; try{ragList.value=await agentApi.searchPrompts({keyword:ragQ.value,page:1,size:10})||[]}catch{ragList.value=[]}finally{ragBusy.value=false} }
async function doReindex() { try{await (agentApi as any).reindexPrompts?.()}catch{} }
function parseTags(raw:string){ try{return JSON.parse(raw)}catch{return raw?.split(',')||[]} }

// ── ReAct ──
const reactTask = ref(''); const reactBusy = ref(false); const reactTrace = ref<{type:string;content:string}[]>([])
async function doReAct() {
  if(!reactTask.value||reactBusy.value)return; reactBusy.value=true; reactTrace.value=[]
  try{
    const token = localStorage.getItem('token')
    const r = await fetch('/api/v1/agent/execute?task='+encodeURIComponent(reactTask.value),{method:'POST',headers:{Authorization:'Bearer '+token}})
    const reader = r.body?.getReader(); if(!reader){reactBusy.value=false;return}
    const dec = new TextDecoder(); let buf = ''
    while(true){ const{value,done}=await reader.read(); if(done)break
      buf+=dec.decode(value,{stream:true})
      for(const line of buf.split('\n')){
        if(line.startsWith('data:')){
          const d=line.slice(5).trim(); if(d==='[DONE]'){reactBusy.value=false;return}
          try{const j=JSON.parse(d);reactTrace.value.push({type:j.type||'info',content:j.content||j.message||d})}catch{reactTrace.value.push({type:'raw',content:d})}
        }
      }
      buf=buf.includes('\n')?buf.split('\n').pop()||'':buf
    }
  }catch(e:any){reactTrace.value.push({type:'error',content:e.message})}finally{reactBusy.value=false}
}

// ── Multi-Agent ──
const multiTask = ref(''); const multiBusy = ref(false); const multiTrace = ref<{agent:string;content:string}[]>([])
async function doMulti() {
  if(!multiTask.value||multiBusy.value)return; multiBusy.value=true; multiTrace.value=[]
  try{
    const token = localStorage.getItem('token')
    const r = await fetch('/api/v1/agent/workflow?task='+encodeURIComponent(multiTask.value),{method:'POST',headers:{Authorization:'Bearer '+token}})
    const reader = r.body?.getReader(); if(!reader){multiBusy.value=false;return}
    const dec = new TextDecoder(); let buf = ''
    while(true){ const{value,done}=await reader.read(); if(done)break
      buf+=dec.decode(value,{stream:true})
      for(const line of buf.split('\n')){
        if(line.startsWith('data:')){
          try{const j=JSON.parse(line.slice(5).trim());multiTrace.value.push({agent:j.agent||'WORKER',content:j.content||j.message||''})}catch{}
        }
      }
      buf=buf.includes('\n')?buf.split('\n').pop()||'':buf
    }
  }catch(e:any){multiTrace.value.push({agent:'ERROR',content:e.message})}finally{multiBusy.value=false}
}

onMounted(async()=>{ try{toolList.value=await (agentApi as any).listTools?.()||[]}catch{} })
</script>
