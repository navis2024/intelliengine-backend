import { test, expect } from '@playwright/test'

const USER = 'e2e_test'
const PASS = 'test123456'

test.describe('草稿区即梦视频完整检查', () => {

  test('登录 → API验证 → 视频播放URL → 帧数据', async ({ page, request }) => {
    // ── Login ──
    const loginResp = await request.post('http://localhost:8081/api/v1/users/login', {
      data: { username: USER, password: PASS },
    })
    expect(loginResp.ok()).toBeTruthy()
    const loginBody = await loginResp.json()
    expect(loginBody.success).toBe(true)
    const token = loginBody.data.token
    const auth = { Authorization: `Bearer ${token}` }
    console.log('1. Login: OK')

    // ── Project 14 ──
    const projResp = await request.get('http://localhost:8081/api/v1/projects/14', { headers: auth })
    const proj = await projResp.json()
    expect(proj.success).toBe(true)
    console.log(`2. Project: ${proj.data.name} (id=${proj.data.id})`)

    // ── AI Videos in Project 14 ──
    const videosResp = await request.get('http://localhost:8081/api/v1/agent/videos?projectId=14', { headers: auth })
    const videos = await videosResp.json()
    expect(videos.success).toBe(true)
    expect(videos.data.length).toBeGreaterThan(0)
    const video = videos.data[0]
    console.log(`3. AI Video: ${video.toolType} v${video.toolVersion} fps=${video.fps}`)
    console.log(`   Prompt: ${video.promptText?.substring(0, 60)}`)

    // ── Frames ──
    const framesResp = await request.get(`http://localhost:8081/api/v1/agent/videos/${video.id}/frames`, { headers: auth })
    const frames = await framesResp.json()
    expect(frames.success).toBe(true)
    expect(frames.data.length).toBe(7)
    console.log(`4. Frames: ${frames.data.length}`)
    let keyframes = 0
    for (const f of frames.data) {
      const kf = f.isKeyframe ? ' ★KEY' : ''
      if (f.isKeyframe) keyframes++
      console.log(`   #${f.frameNumber} @${f.timestamp}s${kf}: ${f.promptText?.substring(0, 50)}`)
    }
    expect(keyframes).toBe(7) // all 7 are real FFmpeg I-frames

    // ── Asset & Play URL ──
    const assetResp = await request.get('http://localhost:8081/api/v1/assets/28', { headers: auth })
    const asset = await assetResp.json()
    expect(asset.success).toBe(true)
    expect(asset.data.ownerType).toBe('PROJECT')
    expect(Number(asset.data.ownerId)).toBe(14)
    expect(asset.data.status).toBe('READY')
    console.log(`5. Asset: ${asset.data.name} | file: ${asset.data.fileUrl?.substring(0, 40)} | status: ${asset.data.status}`)

    const playResp = await request.get('http://localhost:8081/api/v1/assets/28/play-url', { headers: auth })
    const play = await playResp.json()
    console.log(`6. Play URL success: ${play.success}, url: ${play.data?.substring(0, 80)}`)
    expect(play.success).toBe(true)
    expect(play.data).toBeTruthy()
    expect(play.data).toContain('http')

    // ── Frontend page loads ──
    await page.goto('http://localhost:3000/login', { waitUntil: 'domcontentloaded', timeout: 15000 })
    await page.waitForSelector('.input-field', { timeout: 10000 })
    const inputs = page.locator('.input-field')
    await inputs.nth(0).fill(USER)
    await inputs.nth(1).fill(PASS)
    await page.click('button[type="submit"]')
    await page.waitForTimeout(3000)
    const tokenInBrowser = await page.evaluate(() => localStorage.getItem('token'))
    expect(tokenInBrowser).toBeTruthy()
    console.log('7. Frontend login: OK')

    // ── Navigate to project detail page ──
    await page.goto('http://localhost:3000/projects/14', { waitUntil: 'domcontentloaded', timeout: 15000 })
    await page.waitForTimeout(2000)
    // Button is a <router-link> styled as btn-primary, not a <button>
    const draftLink = page.locator('a.btn-primary:has-text("Open Draft Board")')
    const hasDraftLink = await draftLink.isVisible().catch(() => false)
    console.log(`8. Draft Board link visible: ${hasDraftLink}`)
    if (hasDraftLink) {
      await draftLink.click()
      await page.waitForTimeout(3000)
      const url = page.url()
      console.log(`   Navigated to: ${url}`)
      // Check for video player or frames in draft board
      const hasVideo = await page.locator('video').isVisible().catch(() => false)
      const frameCount = await page.locator('aside .group').count().catch(() => 0)
      console.log(`   Video player visible: ${hasVideo}, Frames in sidebar: ${frameCount}`)
    }

    console.log('\n=== ALL API CHECKS PASSED ===')
  })

})
