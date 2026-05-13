<template>
  <div class="p-8 space-y-6">
    <div v-if="loading" class="text-sm text-muted-foreground">Loading...</div>
    <template v-else-if="project">
      <router-link to="/projects" class="text-xs text-muted-foreground hover:text-foreground transition-colors inline-flex items-center gap-1">← Back to Projects</router-link>

      <!-- Project Header -->
      <div class="flex items-start justify-between">
        <div class="space-y-1">
          <h2 class="text-2xl font-bold tracking-tight font-serif italic">{{ project.name }}</h2>
          <p class="text-sm text-muted-foreground font-mono text-[11px]">{{ project.projectCode }}</p>
        </div>
        <div class="flex items-center gap-3">
          <router-link :to="`/projects/${project.id}/draft`" class="btn-primary gap-2 h-10 px-6 text-sm font-medium">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            Open Draft Board
          </router-link>
          <button @click="handleAddVersion" class="btn-outline gap-2 h-10 px-4 text-sm font-medium">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
            Add Version
          </button>
          <input ref="fileInput" type="file" accept="video/mp4,video/*" class="hidden" @change="onVersionFile" />
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex border-b border-border">
        <button v-for="tab in tabs" :key="tab" @click="activeTab=tab"
          :class="activeTab===tab ? 'border-foreground text-foreground' : 'border-transparent text-muted-foreground hover:text-foreground'"
          class="border-b-2 px-4 pb-3 text-sm font-medium transition-colors -mb-px">{{ tab }}</button>
      </div>

      <!-- Overview -->
      <div v-if="activeTab==='Overview'" class="space-y-6">
        <!-- Description -->
        <div class="rounded-xl border border-border bg-card p-6 space-y-3">
          <h3 class="text-sm font-semibold">Description</h3>
          <p class="text-sm text-muted-foreground">{{ project.description || 'No description yet.' }}</p>
        </div>

        <!-- Linked Assets (sent from Asset Management) -->
        <div class="rounded-xl border border-border bg-card p-6 space-y-4">
          <div class="flex items-center justify-between">
            <h3 class="text-sm font-semibold">Linked Assets</h3>
            <span class="text-xs text-muted-foreground">{{ linkedAssets.length }} assets · integrated by Agent</span>
          </div>
          <div v-if="linkedAssets.length===0" class="text-sm text-muted-foreground text-center py-8">
            <div class="text-3xl mb-2">📂</div>
            <p>No assets linked yet.</p>
            <p class="text-xs text-muted-foreground mt-1">Go to <router-link to="/assets" class="text-blue-400 hover:text-blue-300">Asset Management</router-link> and send assets to this project.</p>
          </div>
          <div v-else class="grid grid-cols-3 gap-3">
            <div v-for="a in linkedAssets" :key="a.id"
              class="rounded-lg border border-border p-3 space-y-1.5 hover:border-foreground/15 transition-all">
              <div class="flex items-center gap-2">
                <span class="text-xl">{{ a.type==='VIDEO'?'🎬':a.type==='IMAGE'?'🖼':a.type==='AUDIO'?'🎵':'📄' }}</span>
                <div class="min-w-0">
                  <p class="text-xs font-medium truncate">{{ a.name }}</p>
                  <p class="text-[10px] text-muted-foreground">{{ a.type }} · {{ a.fileSize ? formatSize(a.fileSize) : '—' }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Agent Integration Status -->
        <div class="rounded-xl border border-border bg-card p-6 space-y-4">
          <h3 class="text-sm font-semibold flex items-center gap-2">
            <Bot class="w-4 h-4 text-blue-400" /> Agent Integration
          </h3>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div class="rounded-lg bg-accent/30 p-4 space-y-1">
              <p class="text-xs text-muted-foreground">Asset Analysis</p>
              <p class="font-bold">{{ linkedAssets.length > 0 ? '✅ ' + linkedAssets.length + ' assets integrated' : '⏳ No assets to analyze' }}</p>
            </div>
            <div class="rounded-lg bg-accent/30 p-4 space-y-1">
              <p class="text-xs text-muted-foreground">Agent Reports</p>
              <p class="font-bold">{{ agentReportCount > 0 ? '✅ ' + agentReportCount + ' reports' : '⏳ None yet' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Version Tree -->
      <div v-else-if="activeTab==='Version Tree'" class="rounded border border-border bg-card p-6">
        <VersionTree :versions="versionTreeData" :loading="versionTreeLoading"
          @select="onVersionSelect" />
      </div>

      <!-- Members -->
      <div v-else-if="activeTab==='Members'" class="space-y-6">
        <div class="flex justify-between items-center">
          <h3 class="text-sm font-semibold">Project Members <span class="text-muted-foreground font-normal">({{ members.length }})</span></h3>
          <button @click="showAddMember=true" class="btn-primary text-xs h-8">Add Member</button>
        </div>
        <div class="rounded border border-border bg-card divide-y divide-border">
          <div v-for="m in members" :key="m.userId" class="flex items-center gap-4 p-4">
            <div class="w-8 h-8 rounded-full bg-muted flex items-center justify-center text-xs font-bold">{{ (m.username || 'U').charAt(0).toUpperCase() }}</div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium">{{ m.username || m.userId }}</p>
              <p class="text-[10px] text-muted-foreground">{{ m.role }}</p>
            </div>
            <select :value="m.role" @change="updateRole(m, ($event.target as HTMLSelectElement).value)" class="text-xs input-field w-24">
              <option value="OWNER">Owner</option><option value="ADMIN">Admin</option><option value="EDITOR">Editor</option><option value="VIEWER">Viewer</option>
            </select>
            <button @click="removeMember(m)" class="text-xs text-destructive hover:text-destructive/80 transition-colors">Remove</button>
          </div>
        </div>

        <!-- Add Member Dialog -->
        <div v-if="showAddMember" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60" @click.self="showAddMember=false">
          <div class="w-full max-w-sm rounded-lg border border-border bg-card p-6 shadow-xl space-y-4">
            <h3 class="text-lg font-semibold">Add Member</h3>
            <div class="space-y-3">
              <div class="space-y-1"><label class="text-[10px] font-medium text-muted-foreground uppercase">User ID</label><input v-model="newMemberId" class="input-field" placeholder="Enter user ID" /></div>
              <div class="space-y-1"><label class="text-[10px] font-medium text-muted-foreground uppercase">Role</label>
                <select v-model="newMemberRole" class="input-field"><option value="MEMBER">Member</option><option value="ADMIN">Admin</option><option value="VIEWER">Viewer</option></select>
              </div>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button @click="showAddMember=false" class="btn-ghost text-sm">Cancel</button>
              <button @click="addMember" class="btn-primary text-sm">Add</button>
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="activeTab==='Settings'" class="rounded border border-border bg-card p-8 space-y-4">
        <h3 class="text-sm font-semibold">Project Settings</h3>
        <form @submit.prevent="handleUpdate" class="space-y-4 max-w-md">
          <div class="space-y-1"><label class="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">Name</label><input v-model="editForm.name" class="input-field" /></div>
          <div class="space-y-1"><label class="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">Description</label><textarea v-model="editForm.description" class="input-field" rows="4" /></div>
          <button type="submit" class="btn-primary">Save Changes</button>
        </form>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { projectApi } from '@/api/project'
import { assetApi } from '@/api/asset'
import { agentApi } from '@/api/agent'
import { Bot } from 'lucide-vue-next'
import VersionTree from '@/components/VersionTree.vue'
import { useRouter } from 'vue-router'

const route = useRoute()
const tabs = ['Overview', 'Version Tree', 'Members', 'Settings']
const activeTab = ref('Overview')
const loading = ref(true)
const project = ref<any>(null)
const editForm = reactive({ name: '', description: '' })
const members = ref<any[]>([])
const showAddMember = ref(false)
const newMemberId = ref('')
const newMemberRole = ref('MEMBER')
const linkedAssets = ref<any[]>([])
const agentReportCount = ref(0)
const router = useRouter()
const versionTreeData = ref<any[]>([])
const versionTreeLoading = ref(false)

async function loadVersionTree() {
  versionTreeLoading.value = true
  try {
    const assetId = linkedAssets.value[0]?.id
    if (!assetId) { versionTreeLoading.value = false; return }
    const versions = await assetApi.getAssetVersions(assetId)
    versionTreeData.value = (versions || []).reverse()
  } catch { versionTreeData.value = [] }
  finally { versionTreeLoading.value = false }
}

function onVersionSelect(v: any) {
  router.push(`/projects/${project.value.id}/draft?version=${v.versionNumber}`)
}

function handleAddVersion() { fileInput.value?.click() }

async function onVersionFile(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !project.value?.id) return
  try {
    // Find the first linked asset to add version to
    const assetId = linkedAssets.value[0]?.id
    if (!assetId) { alert('No linked asset found in this project'); return }
    const formData = new FormData()
    formData.append('file', file)
    formData.append('assetId', String(assetId))
    await assetApi.uploadFile(file, { name: file.name, type: 'VIDEO', ownerType: 'PROJECT', ownerId: String(project.value.id) })
    alert('New version uploaded!')
    location.reload()
  } catch (e: any) { alert('Upload failed: ' + (e?.message || 'Unknown error')) }
  finally { input.value = '' }
}

const versionTree = [
  { version: 3, title: '帧#3-#5 批注修改 (当前)', author: 'Agent', time: '2 hours ago', current: true },
  { version: 2, title: 'Agent 第一版全局分析', author: 'Agent', time: 'Yesterday', current: false },
  { version: 1, title: '初始上传', author: '成员·李', time: '2 days ago', current: false },
]

const formatSize = (bytes: number) => bytes > 1048576 ? `${(bytes/1048576).toFixed(1)} MB` : `${(bytes/1024).toFixed(0)} KB`

onMounted(async () => {
  try {
    const projectId = route.params.id as string
    const [p, m] = await Promise.all([
      projectApi.getProject(projectId),
      projectApi.getProjectMembers(projectId),
    ])
    project.value = p; members.value = m || []
    editForm.name = p.name; editForm.description = p.description || ''

    // Load linked assets for this project
    try {
      const assets = await assetApi.listAssets({ ownerId: projectId, ownerType: 'PROJECT' })
      linkedAssets.value = (assets as any)?.list || []
    } catch {}

    loadVersionTree()

    // Load agent reports for this project
    try {
      const reports = await agentApi.listReports(Number(projectId), 1, 20)
      agentReportCount.value = (reports as any)?.length || 0
    } catch {}
  } catch {} finally { loading.value = false }
})

const handleUpdate = async () => {
  try { await projectApi.updateProject(route.params.id as string, editForm); project.value.name = editForm.name; project.value.description = editForm.description } catch {}
}

const addMember = async () => {
  try { await projectApi.addProjectMember(route.params.id as string, { userId: newMemberId.value, role: newMemberRole.value }); showAddMember.value = false; newMemberId.value = ''; const m = await projectApi.getProjectMembers(route.params.id as string); members.value = m || [] } catch {}
}
const updateRole = async (member: any, role: string) => {
  try { await projectApi.updateProjectMember(route.params.id as string, member.userId, { role }); member.role = role } catch {}
}
const removeMember = async (member: any) => {
  try { await projectApi.removeProjectMember(route.params.id as string, member.userId); members.value = members.value.filter(x => x.userId !== member.userId) } catch {}
}
</script>
