import { writeFileSync, unlinkSync } from 'fs';
import { execSync } from 'child_process';

const KIMI_KEY = 'sk-zgVvhNHyJpEhCclHxQ8VLKeHrW6OP3k69Ypc6GaP7syFWHXt';
const API = 'http://localhost:8081/api/v1';
const MINIO = 'http://localhost:9000';

// Read frame thumbnail from MinIO via mc cat (returns raw JPEG bytes)
function downloadFromMinio(objectPath) {
  const data = execSync(`docker exec intelliengine-minio mc cat myminio/jimeng/${objectPath}`, { timeout: 15000, maxBuffer: 5*1024*1024 });
  if (data.length > 100) return data;
  return null;
}

async function login() {
  const r = await fetch(`${API}/users/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'e2e_test', password: 'test123456' }),
  });
  return (await r.json()).data.token;
}

const VIDEO_ID = parseInt(process.argv[2] || '5');

async function getFrames(token) {
  const r = await fetch(`${API}/agent/videos/${VIDEO_ID}/frames`, { headers: { Authorization: `Bearer ${token}` } });
  return (await r.json()).data || [];
}

async function analyzeImage(base64Data, ctx) {
  const dataUrl = 'data:image/jpeg;base64,' + base64Data;
  const body = {
    model: 'kimi-k2.5', temperature: 1.0, max_tokens: 500,
    messages: [
      { role: 'system', content: '你是一个专业的视频画面分析助手。请用简洁的中文描述视频帧的画面内容，包括：镜头类型、场景、主体、光线、色调。50字以内。' },
      { role: 'user', content: [{ type: 'image_url', image_url: { url: dataUrl } }, { type: 'text', text: `描述这个视频帧的画面内容(${ctx})：` }] },
    ],
  };
  const r = await fetch('https://api.moonshot.cn/v1/chat/completions', {
    method: 'POST', headers: { 'Authorization': `Bearer ${KIMI_KEY}`, 'Content-Type': 'application/json' },
    body: JSON.stringify(body), signal: AbortSignal.timeout(120000),
  });
  const j = await r.json();
  if (r.ok && j.choices?.length) {
    const msg = j.choices[0].message;
    // K2.5 reasoning model: final answer in content, thinking in reasoning_content
    return (msg.content || msg.reasoning_content || '').trim();
  }
  console.log(`    KIMI ${r.status}: ${JSON.stringify(j.error || j).substring(0, 200)}`);
  return null;
}

async function main() {
  console.log('Login...');
  const token = await login();

  console.log('Fetch frames...');
  const frames = await getFrames(token);
  console.log(`Got ${frames.length} frames\n`);

  const sqls = [];
  let ok = 0;

  for (const f of frames) {
    if (!f.thumbnailUrl) { console.log(`  #${f.frameNumber}: skip`); continue; }
    console.log(`  #${f.frameNumber} @${f.timestamp}s...`);

    try {
      const objectPath = `frames/${VIDEO_ID}/thumb_${f.frameNumber}.jpg`;
      const imgData = downloadFromMinio(objectPath);

      if (!imgData) { console.log('    → MinIO download failed'); continue; }

      const base64Data = imgData.toString('base64');
      console.log(`    image: ${(imgData.length/1024).toFixed(0)}KB`);

      const desc = await analyzeImage(base64Data, `帧#${f.frameNumber} 时间${f.timestamp}秒`);
      if (desc) {
        console.log(`    → ${desc}`);
        const safe = desc.replace(/'/g, "''");
        sqls.push(`UPDATE video_frame SET prompt_text = '${safe}' WHERE id = ${f.id};`);
        ok++;
      }
    } catch (e) {
      console.log(`    → ERROR: ${e.message}`);
    }
    await new Promise(r => setTimeout(r, 3000));
  }

  if (sqls.length) {
    const tmp = 'E:/pj/ZQ_plat/scripts/_fix_prompts.sql';
    writeFileSync(tmp, sqls.join('\n'), 'utf-8');
    execSync(`docker exec -i intelliengine-mysql mysql -uroot -pIntelliEngine@2025 intelliengine < "${tmp}"`, { stdio: 'pipe' });
    unlinkSync(tmp);
  }
  console.log(`\nDone: ${ok}/${frames.length} frames analyzed`);
}

main().catch(e => { console.error('FATAL:', e.message); process.exit(1); });
