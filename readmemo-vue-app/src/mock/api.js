// 模拟 API 函数
export const mockDocumentAPI = {
  // 获取书架文档列表
  fetchAll: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          {
            id: 1,
            title: 'The Great Gatsby',
            author: 'F. Scott Fitzgerald',
            cover: 'https://picsum.photos/seed/book1/200/300',
            status: 'reading',
            progress: 65,
            tags: ['小说', '经典'],
            lastRead: '2025-12-15'
          },
          {
            id: 2,
            title: 'To Kill a Mockingbird',
            author: 'Harper Lee',
            cover: 'https://picsum.photos/seed/book2/200/300',
            status: 'processed',
            progress: 100,
            tags: ['小说', '社会'],
            lastRead: '2025-12-10'
          },
          {
            id: 3,
            title: '1984',
            author: 'George Orwell',
            cover: 'https://picsum.photos/seed/book3/200/300',
            status: 'processing',
            progress: 30,
            tags: ['反乌托邦', '政治'],
            lastRead: null
          },
          {
            id: 4,
            title: 'Pride and Prejudice',
            author: 'Jane Austen',
            cover: 'https://picsum.photos/seed/book4/200/300',
            status: 'unprocessed',
            progress: 0,
            tags: ['爱情', '经典'],
            lastRead: null
          },
          {
            id: 5,
            title: 'The Catcher in the Rye',
            author: 'J.D. Salinger',
            cover: 'https://picsum.photos/seed/book5/200/300',
            status: 'reading',
            progress: 45,
            tags: ['成长', '小说'],
            lastRead: '2025-12-14'
          },
          {
            id: 6,
            title: 'Brave New World',
            author: 'Aldous Huxley',
            cover: 'https://picsum.photos/seed/book6/200/300',
            status: 'processed',
            progress: 100,
            tags: ['科幻', '反乌托邦'],
            lastRead: '2025-12-05'
          }
        ])
      }, 500)
    })
  },

  // 根据状态筛选
  fetchByStatus: (status) => {
    return mockDocumentAPI.fetchAll().then(docs => {
      if (status === 'all') return docs
      return docs.filter(doc => doc.status === status)
    })
  },

  // 删除文档
  deleteDocument: (id) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log(`Deleted document ${id}`)
        resolve({ success: true })
      }, 300)
    })
  },

  // 更新进度
  updateProgress: (id, progress) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log(`Updated document ${id} progress to ${progress}%`)
        resolve({ success: true })
      }, 300)
    })
  }
}

export const mockUploadAPI = {
  // 模拟上传
  uploadFile: (file) => {
    return new Promise((resolve) => {
      const total = 100
      let progress = 0
      const interval = setInterval(() => {
        progress += 10
        if (progress >= total) {
          clearInterval(interval)
          resolve({
            id: Date.now(),
            name: file.name,
            size: file.size,
            url: 'https://example.com/uploaded/' + file.name
          })
        }
      }, 200)
    })
  }
}

export const mockNotificationAPI = {
  // 获取通知
  getNotifications: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          { id: 1, type: 'upload', message: '文档 "1984" 上传成功', time: '5分钟前', read: false },
          { id: 2, type: 'process', message: '文档 "The Great Gatsby" 处理完成', time: '1小时前', read: true },
          { id: 3, type: 'review', message: '你有 5 个生词需要复习', time: '2小时前', read: false },
          { id: 4, type: 'system', message: '系统维护通知：本周六凌晨2-4点', time: '1天前', read: true }
        ])
      }, 500)
    })
  },

  // 标记为已读
  markAsRead: (id) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log(`Marked notification ${id} as read`)
        resolve({ success: true })
      }, 200)
    })
  }
}

export const mockUserAPI = {
  getProfile: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          name: '阅读达人',
          avatar: 'https://picsum.photos/seed/avatar/100/100',
          email: 'user@example.com',
          joinDate: '2025-01-15',
          stats: {
            totalBooks: 42,
            totalWords: 1250,
            readingDays: 156,
            currentStreak: 7
          }
        })
      }, 500)
    })
  }
}