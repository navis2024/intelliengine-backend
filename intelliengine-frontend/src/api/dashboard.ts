import request from '@/utils/request'

export interface DashboardStats {
  totalProjects: number
  totalAssets: number
  recentAssets: Array<{
    id: string; name: string; type: string; status: string
    fileUrl: string; fileSize: number; createTime: string
  }>
}

export const dashboardApi = {
  getStats(): Promise<DashboardStats> {
    return request.get('/dashboard/stats', { unwrap: true })
  },
}
