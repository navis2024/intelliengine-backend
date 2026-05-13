<template>
  <div class="p-8 space-y-8">
    <div>
      <h2 class="text-2xl font-bold tracking-tight">Dashboard</h2>
      <p class="text-sm text-muted-foreground mt-1">Real-time overview of your projects, assets, and agent activity</p>
    </div>

    <!-- Stats Overview -->
    <div class="grid grid-cols-4 gap-4">
      <div v-for="s in stats" :key="s.label" class="rounded-xl border border-border bg-card p-5 space-y-2 hover:border-foreground/10 transition-all">
        <div class="flex items-center gap-2">
          <component :is="s.icon" class="w-4 h-4 text-muted-foreground" />
          <p class="text-xs font-medium text-muted-foreground uppercase tracking-wider">{{ s.label }}</p>
        </div>
        <p class="text-3xl font-bold">{{ s.value }}</p>
        <p class="text-[10px] text-muted-foreground">{{ s.subtitle }}</p>
      </div>
    </div>

    <div class="grid grid-cols-3 gap-6">
      <!-- Recent Projects -->
      <div class="col-span-2 space-y-4">
        <div class="rounded-xl border border-border bg-card p-6">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-sm font-semibold">Recent Projects</h3>
            <router-link to="/projects" class="text-xs text-muted-foreground hover:text-foreground transition-colors">View all →</router-link>
          </div>
          <div v-if="recentProjects.length===0" class="text-sm text-muted-foreground text-center py-12">
            <div class="text-4xl mb-3">📂</div>
            <p>No projects yet.</p>
            <router-link to="/projects" class="text-xs text-blue-400 hover:text-blue-300 mt-1 inline-block">Create your first project</router-link>
          </div>
          <div v-else class="space-y-3">
            <router-link v-for="p in recentProjects" :key="p.id" :to="`/projects/${p.id}`"
              class="flex items-center gap-4 p-4 rounded-lg border border-border hover:border-foreground/15 hover:bg-accent/30 transition-all no-underline">
              <div class="w-10 h-10 rounded-lg bg-muted flex items-center justify-center text-lg font-bold text-muted-foreground">
                {{ (p.name || 'P').charAt(0).toUpperCase() }}
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-foreground truncate">{{ p.name }}</p>
                <p class="text-xs text-muted-foreground">{{ p.description || 'No description' }}</p>
              </div>
              <div class="flex items-center gap-3 text-xs text-muted-foreground shrink-0">
                <span>{{ p.memberCount || 1 }} members</span>
                <span>{{ p.assetCount || 0 }} assets</span>
                <span class="text-[10px] px-2 py-0.5 rounded-full" :class="p.status==='ACTIVE'?'bg-green-500/20 text-green-300':'bg-muted text-muted-foreground'">{{ p.status }}</span>
              </div>
            </router-link>
          </div>
        </div>

        <!-- Agent Activity Feed -->
        <div class="rounded-xl border border-border bg-card p-6">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-sm font-semibold flex items-center gap-2">
              <Bot class="w-4 h-4 text-blue-400" /> Agent Activity
            </h3>
            <router-link to="/agent" class="text-xs text-muted-foreground hover:text-foreground transition-colors">Agent Center →</router-link>
          </div>
          <div class="space-y-3">
            <div v-for="(a, i) in agentActivities" :key="i"
              class="flex items-start gap-3 p-3 rounded-lg hover:bg-accent/30 transition-colors">
              <div class="w-2 h-2 rounded-full mt-1.5 shrink-0"
                :class="a.type==='analysis' ? 'bg-blue-400' : a.type==='report' ? 'bg-green-400' : a.type==='anomaly' ? 'bg-red-400' : 'bg-muted-foreground'" />
              <div class="flex-1 min-w-0">
                <p class="text-sm">{{ a.text }}</p>
                <p class="text-xs text-muted-foreground mt-0.5">{{ a.detail }}</p>
              </div>
              <span class="text-[10px] text-muted-foreground shrink-0">{{ a.time }}</span>
            </div>
            <div v-if="agentActivities.length===0" class="text-sm text-muted-foreground text-center py-6">
              Agent will show activity here once you start analyzing assets and generating reports.
            </div>
          </div>
        </div>
      </div>

      <!-- Right Sidebar -->
      <div class="space-y-4">
        <!-- Quick Actions -->
        <div class="rounded-xl border border-border bg-card p-6">
          <h3 class="text-sm font-semibold mb-4">Quick Actions</h3>
          <div class="space-y-2">
            <router-link v-for="a in actions" :key="a.label" :to="a.path"
              class="flex items-center gap-3 rounded-lg border border-border px-4 py-3 text-sm font-medium hover:bg-accent transition-colors no-underline group">
              <component :is="a.icon" class="w-4 h-4 text-muted-foreground group-hover:text-foreground transition-colors" />
              <div>
                <p class="text-sm">{{ a.label }}</p>
                <p class="text-[10px] text-muted-foreground">{{ a.desc }}</p>
              </div>
            </router-link>
          </div>
        </div>

        <!-- Project Stats Breakdown -->
        <div class="rounded-xl border border-border bg-card p-6 space-y-4">
          <h3 class="text-sm font-semibold">Project Breakdown</h3>
          <div class="space-y-3 text-sm">
            <div class="flex justify-between items-center">
              <span class="text-muted-foreground">Active</span>
              <span class="font-bold text-green-400">{{ activeProjectCount }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-muted-foreground">Archived</span>
              <span class="font-bold text-muted-foreground">{{ archivedProjectCount }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-muted-foreground">Total Assets</span>
              <span class="font-bold">{{ stats[1]?.value || '—' }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-muted-foreground">Templates</span>
              <span class="font-bold">{{ stats[2]?.value || '—' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FolderKanban, Image, ShoppingBag, Play, Bot } from 'lucide-vue-next'
import { projectApi } from '@/api/project'
import { marketApi } from '@/api/market'
import { agentApi } from '@/api/agent'
import { assetApi } from '@/api/asset'
import { dashboardApi } from '@/api/dashboard'

const stats = ref([
  { label: 'Projects', value: '—', subtitle: 'active collaborations', icon: FolderKanban },
  { label: 'Assets', value: '—', subtitle: 'videos, images, audio', icon: Image },
  { label: 'Templates', value: '—', subtitle: 'market collection', icon: ShoppingBag },
  { label: 'Prompts', value: '—', subtitle: 'AI prompt library', icon: Play },
])
const recentProjects = ref<any[]>([])
const agentActivities = ref<{ type: string; text: string; detail: string; time: string }[]>([])
const activeProjectCount = ref(0)
const archivedProjectCount = ref(0)

onMounted(async () => {
  // Load stats
  try {
    const [dashStats, templates, prompts, allProjects] = await Promise.all([
      dashboardApi.getStats(),
      marketApi.getTemplates({ page: 1, size: 1 }),
      agentApi.searchPrompts({ page: 1, size: 1 }),
      projectApi.getMyProjects({ pageNum: 1, pageSize: 50 }),
    ])
    stats.value[0].value = String(dashStats?.totalProjects || 0)
    stats.value[1].value = String(dashStats?.totalAssets || 0)
    stats.value[2].value = String((templates as any)?.total || 0)
    stats.value[3].value = String((prompts as any)?.length || 0)

    const projectList = (allProjects as any)?.list || []
    activeProjectCount.value = projectList.filter((p: any) => p.status === 'ACTIVE').length
    archivedProjectCount.value = projectList.filter((p: any) => p.status === 'ARCHIVED').length

    recentProjects.value = projectList.slice(0, 5)
  } catch { /* use fallback */ }

  // Load agent activities
  try {
    const [tasks, reports] = await Promise.all([
      agentApi.listTasks(1, 5),
      agentApi.listReports(undefined, 1, 5),
    ])
    const taskList = (tasks as any) || []
    const reportList = (reports as any) || []
    const activities: { type: string; text: string; detail: string; time: string }[] = []

    taskList.forEach((t: any) => {
      activities.push({
        type: t.status === 'COMPLETED' ? 'analysis' : t.status === 'FAILED' ? 'anomaly' : 'task',
        text: `Data task "${t.name}" ${t.status === 'COMPLETED' ? 'completed' : t.status === 'FAILED' ? 'failed' : 'running'}`,
        detail: `Platform: ${t.platform} · Status: ${t.status}`,
        time: t.lastExecuteTime?.substring(0, 16) || t.createdAt?.substring(0, 16) || '—',
      })
    })
    reportList.forEach((r: any) => {
      activities.push({
        type: 'report',
        text: `Report "${r.title}" generated`,
        detail: `Type: ${r.type}`,
        time: r.generatedAt?.substring(0, 16) || r.createdAt?.substring(0, 16) || '—',
      })
    })
    // Sort by time desc
    activities.sort((a, b) => b.time.localeCompare(a.time))
    agentActivities.value = activities.slice(0, 6)
  } catch {}
})

const actions = [
  { label: 'New Project', desc: 'Start a collaboration space', path: '/projects', icon: FolderKanban },
  { label: 'Upload Asset', desc: 'Upload videos, images, audio', path: '/assets', icon: Image },
  { label: 'Browse Market', desc: 'Discover AIGC templates', path: '/market', icon: ShoppingBag },
  { label: 'Agent Center', desc: 'Prompts, analysis, reports', path: '/agent', icon: Bot },
]
</script>
