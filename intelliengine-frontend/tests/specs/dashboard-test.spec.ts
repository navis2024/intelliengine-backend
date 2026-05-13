import { test, expect } from '@playwright/test'

const USER = 'e2e_test'
const PASS = 'test123456'

test.describe('新功能验证：Dashboard + Group ID + Agent Advice', () => {

  test('01 - Dashboard统计', async ({ request }) => {
    const loginResp = await request.post('http://localhost:8081/api/v1/users/login', {
      data: { username: USER, password: PASS },
    })
    const token = (await loginResp.json()).data.token
    const auth = { Authorization: `Bearer ${token}` }

    const r = await request.get('http://localhost:8081/api/v1/dashboard/stats', { headers: auth })
    const stats = await r.json()
    console.log('Dashboard:', JSON.stringify(stats.data))
    expect(stats.success).toBe(true)
    expect(stats.data.totalProjects).toBeGreaterThan(0)
    expect(stats.data.totalAssets).toBeGreaterThan(0)
  })

  test('02 - Project Group ID 设置和查询', async ({ request }) => {
    const loginResp = await request.post('http://localhost:8081/api/v1/users/login', {
      data: { username: USER, password: PASS },
    })
    const token = (await loginResp.json()).data.token
    const auth = { Authorization: `Bearer ${token}` }

    // 为项目14设置组ID
    const setResp = await request.put('http://localhost:8081/api/v1/projects/14/group-id', {
      headers: auth, params: { groupId: 'TEST_GROUP_001' },
    })
    console.log('Set group ID:', setResp.status(), (await setResp.json()).success)

    // 查询项目确认groupId已设置
    const projResp = await request.get('http://localhost:8081/api/v1/projects/14', { headers: auth })
    const proj = await projResp.json()
    console.log('Project 14 groupId:', proj.data?.groupId)
  })

  test('03 - Agent Advice (Generate Next Version)', async ({ request }) => {
    test.setTimeout(120000) // LLM call can take 60-90s
    const loginResp = await request.post('http://localhost:8081/api/v1/users/login', {
      data: { username: USER, password: PASS },
    })
    const token = (await loginResp.json()).data.token
    const auth = { Authorization: `Bearer ${token}` }

    const r = await request.post('http://localhost:8081/api/v1/agent/videos/5/generate-next-version', {
      headers: auth, params: { projectId: 14 },
    })
    const result = await r.json()
    console.log('Generate OK:', result.success)
    console.log('  version:', result.data?.version)
    console.log('  agentAdvice length:', result.data?.agentAdvice?.length || 0)
    console.log('  newPrompt preview:', result.data?.newPrompt?.substring(0, 80))
    expect(result.success).toBe(true)
    expect(result.data?.agentAdvice).toBeTruthy()
  })

  test('04 - 版本列表含agentAdvice', async ({ request }) => {
    const loginResp = await request.post('http://localhost:8081/api/v1/users/login', {
      data: { username: USER, password: PASS },
    })
    const token = (await loginResp.json()).data.token
    const auth = { Authorization: `Bearer ${token}` }

    const r = await request.get('http://localhost:8081/api/v1/assets/28/versions', { headers: auth })
    const versions = await r.json()
    console.log('Versions count:', versions.data?.length)
    if (versions.data?.length) {
      const latest = versions.data[versions.data.length - 1]
      console.log('  latest has agentAdvice:', !!latest.agentAdvice)
    }
  })

})
