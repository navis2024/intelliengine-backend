<template>
  <div class="version-tree space-y-3">
    <h3 class="text-xs font-semibold uppercase tracking-widest text-muted-foreground flex items-center gap-2">
      <GitBranch class="w-3.5 h-3.5" /> Version Tree
      <span class="font-normal normal-case tracking-normal text-[10px]">{{ versions.length }} versions</span>
    </h3>

    <div v-if="loading" class="text-xs text-muted-foreground py-4 text-center">Loading...</div>

    <div v-else-if="!versions.length" class="text-xs text-muted-foreground py-4 text-center">
      No versions yet. Use "Generate Next Version" or "Add Version" to create one.
    </div>

    <div v-else class="relative pl-5 space-y-0">
      <!-- Timeline line -->
      <div class="absolute left-[7px] top-2 bottom-2 w-px bg-border" />

      <div v-for="(v, i) in versions" :key="v.id"
        class="relative pb-4 last:pb-0 group">
        <!-- Dot -->
        <div class="absolute left-[-17px] top-1.5 w-[15px] h-[15px] rounded-full border-2 flex items-center justify-center transition-all cursor-pointer"
          :class="i === 0 ? 'bg-blue-500 border-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]' : 'bg-card border-border hover:border-blue-500/50'"
          @click="$emit('select', v)">
          <div v-if="i === 0" class="w-1.5 h-1.5 rounded-full bg-white" />
        </div>

        <!-- Version card -->
        <div class="rounded-lg border p-3 cursor-pointer transition-all hover:border-blue-500/30"
          :class="i === 0 ? 'border-blue-500/30 bg-blue-500/5' : 'border-border bg-card/50'"
          @click="$emit('select', v)">

          <div class="flex items-center justify-between mb-1.5">
            <div class="flex items-center gap-2">
              <span class="text-sm font-bold font-mono" :class="i === 0 ? 'text-blue-400' : 'text-foreground'">
                v{{ v.versionNumber }}
              </span>
              <span v-if="i === 0" class="text-[9px] px-1.5 py-0.5 rounded-full bg-blue-500/20 text-blue-400 font-medium">LATEST</span>
            </div>
            <span class="text-[10px] text-muted-foreground font-mono">
              {{ v.createdAt ? new Date(v.createdAt).toLocaleDateString('zh-CN', {month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}) : '' }}
            </span>
          </div>

          <p v-if="v.changeLog" class="text-xs text-foreground/70 leading-relaxed mb-2">
            {{ v.changeLog }}
          </p>

          <!-- Agent Advice expandable -->
          <div v-if="v.agentAdvice" class="mt-2">
            <button @click.stop="expanded[v.id] = !expanded[v.id]"
              class="text-[10px] text-blue-400 hover:text-blue-300 flex items-center gap-1 transition-colors">
              <Bot class="w-3 h-3" />
              {{ expanded[v.id] ? 'Hide' : 'Show' }} Agent Advice
              <span class="transition-transform" :class="expanded[v.id] ? 'rotate-90' : ''">▶</span>
            </button>
            <div v-if="expanded[v.id]" class="mt-2 p-3 rounded bg-blue-500/10 border border-blue-500/20 text-xs space-y-1.5">
              <template v-if="parsedAdvice(v.agentAdvice)">
                <p class="text-foreground/80 leading-relaxed">{{ parsedAdvice(v.agentAdvice).analysis }}</p>
                <ul v-if="parsedAdvice(v.agentAdvice).suggestions?.length" class="list-disc pl-4 space-y-0.5 text-foreground/60">
                  <li v-for="(s, si) in parsedAdvice(v.agentAdvice).suggestions" :key="si">{{ s }}</li>
                </ul>
              </template>
              <p v-else class="text-muted-foreground">{{ v.agentAdvice.substring(0, 200) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { GitBranch, Bot } from 'lucide-vue-next'

const props = defineProps<{ versions: any[]; loading?: boolean }>()
defineEmits<{ select: [version: any] }>()

const expanded = ref<Record<number, boolean>>({})

function parsedAdvice(raw: string) {
  try { return typeof raw === 'string' ? JSON.parse(raw) : raw }
  catch { return null }
}
</script>
