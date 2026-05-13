import { readFileSync, writeFileSync } from 'fs';

let c = readFileSync('src/views/DraftBoard.vue', 'utf8');
const marker = '// ==================== Agent Integration ====================';
const endMarker = 'const generateNextVersion = async () => {';
const startIdx = c.indexOf(marker);
const endIdx = c.indexOf(endMarker, startIdx);

const replacement = marker + `
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

`;

c = c.substring(0, startIdx) + replacement + c.substring(endIdx);
writeFileSync('src/views/DraftBoard.vue', c, 'utf8');
console.log('Done');
