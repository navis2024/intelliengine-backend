<template>
  <div class="review-panel">
    <div class="panel-header">
      <h3 class="text-lg font-semibold">评审评论</h3>
      <span class="text-sm text-gray-500" v-if="comments.length">{{ comments.length }} 条评论</span>
    </div>

    <!-- Add comment -->
    <div class="comment-input">
      <textarea v-model="newComment" rows="2" placeholder="添加评审意见..." class="w-full rounded border border-border bg-muted text-foreground p-2 text-sm placeholder:text-muted-foreground focus:outline-none focus:border-blue-500/50" />
      <button @click="submitComment" :disabled="!newComment.trim()" class="btn-primary mt-2 text-xs px-3 py-1.5 rounded bg-foreground text-background hover:opacity-80 disabled:opacity-40 transition-opacity">
        提交评论
      </button>
    </div>

    <!-- Comment list -->
    <div v-if="comments.length" class="comment-list mt-4 space-y-3">
      <div v-for="c in comments" :key="c.id" class="comment-card rounded border p-3">
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <p class="text-sm whitespace-pre-wrap">{{ c.content }}</p>
            <div class="text-xs text-gray-400 mt-1">
              帧 #{{ c.frameNumber || '-' }} · {{ c.createdAt || '' }}
            </div>
          </div>
          <div class="flex gap-1 ml-2">
            <button v-if="c.status !== 'RESOLVED'" @click="updateStatus(c.id, 'RESOLVED')" class="text-xs text-green-600 hover:underline">解决</button>
            <button @click="deleteComment(c.id)" class="text-xs text-red-500 hover:underline ml-1">删除</button>
          </div>
        </div>

        <!-- Replies -->
        <div v-if="c.replies?.length" class="replies ml-4 mt-2 border-l-2 pl-3 space-y-1">
          <div v-for="r in c.replies" :key="r.id" class="text-sm">
            <p class="text-gray-600">{{ r.content }}</p>
            <div class="flex gap-1">
              <span class="text-xs text-gray-400">{{ r.createdAt || '' }}</span>
              <button @click="deleteReply(r.id)" class="text-xs text-red-400 hover:underline">删除</button>
            </div>
          </div>
        </div>

        <!-- Reply input -->
        <div class="reply-input mt-2 flex gap-2">
          <input v-model="replyTexts[c.id]" placeholder="回复..." class="flex-1 rounded border border-border bg-muted text-foreground px-2 py-1 text-sm placeholder:text-muted-foreground focus:outline-none" />
          <button @click="submitReply(c.id)" :disabled="!replyTexts[c.id]?.trim()" class="text-xs text-blue-400 hover:text-blue-300 disabled:opacity-40">回复</button>
        </div>
      </div>
    </div>

    <div v-else class="text-center text-gray-400 py-6 text-sm">暂无评论，添加第一条评审意见</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { reviewApi } from '@/api/review'
import type { ReviewCommentVO } from '@/types/review'
import { ElMessage } from 'element-plus'

const props = defineProps<{ assetId: number | null; projectId: number | null }>()

const comments = ref<ReviewCommentVO[]>([])
const newComment = ref('')
const replyTexts = ref<Record<number, string>>({})

watch(() => props.assetId, (id) => { if (id) loadComments() }, { immediate: true })

async function loadComments() {
  if (!props.assetId) return
  try {
    comments.value = await reviewApi.getCommentsByAsset(props.assetId)
  } catch { /* fail silently */ }
}

async function submitComment() {
  if (!props.assetId || !newComment.value.trim()) return
  try {
    await reviewApi.createComment({ assetId: props.assetId, projectId: props.projectId!, content: newComment.value })
    newComment.value = ''
    ElMessage.success('评论已提交')
    loadComments()
  } catch { ElMessage.error('提交失败') }
}

async function submitReply(commentId: number) {
  const content = replyTexts.value[commentId]?.trim()
  if (!content) return
  try {
    await reviewApi.createReply(commentId, { content })
    replyTexts.value[commentId] = ''
    ElMessage.success('回复已提交')
    loadComments()
  } catch { ElMessage.error('回复失败') }
}

async function updateStatus(commentId: number, status: string) {
  await reviewApi.updateCommentStatus(commentId, status)
  loadComments()
}

async function deleteComment(commentId: number) {
  await reviewApi.deleteComment(commentId)
  ElMessage.success('已删除')
  loadComments()
}

async function deleteReply(replyId: number) {
  await reviewApi.deleteReply(replyId)
  ElMessage.success('已删除')
  loadComments()
}
</script>

<style scoped>
.review-panel { @apply rounded-lg; }
.panel-header { @apply flex justify-between items-center mb-3; }
.comment-card { @apply bg-muted/30; }
</style>
