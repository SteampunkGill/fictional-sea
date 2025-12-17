import { createRouter, createWebHistory } from 'vue-router'

// 导入视图组件（稍后创建）
const Welcome = () => import('@/views/Welcome.vue')
const Onboarding = () => import('@/views/Onboarding.vue')
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')
const ForgotPassword = () => import('@/views/ForgotPassword.vue')
const Bookshelf = () => import('@/views/Bookshelf.vue')
const Upload = () => import('@/views/Upload.vue')
const Reader = () => import('@/views/Reader.vue')
const DictionaryDetail = () => import('@/views/DictionaryDetail.vue')
const Vocabulary = () => import('@/views/Vocabulary.vue')
const Review = () => import('@/views/Review.vue')
const UserCenter = () => import('@/views/UserCenter.vue')
const Settings = () => import('@/views/Settings.vue')
const SearchResults = () => import('@/views/SearchResults.vue')
const OfflineManagement = () => import('@/views/OfflineManagement.vue') // eslint-disable-line no-unused-vars

const routes = [
  {
    path: '/',
    name: 'Welcome',
    component: Welcome
  },
  {
    path: '/onboarding',
    name: 'Onboarding',
    component: Onboarding
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword
  },
  {
    path: '/bookshelf',
    name: 'Bookshelf',
    component: Bookshelf,
    meta: { requiresAuth: true }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: Upload,
    meta: { requiresAuth: true }
  },
  {
    path: '/reader/:id',
    name: 'Reader',
    component: Reader,
    meta: { requiresAuth: true }
  },
  {
    path: '/dictionary/:word',
    name: 'DictionaryDetail',
    component: DictionaryDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/vocabulary',
    name: 'Vocabulary',
    component: Vocabulary,
    meta: { requiresAuth: true }
  },
  {
    path: '/review',
    name: 'Review',
    component: Review,
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: UserCenter,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings
  },
  {
    path: '/search',
    name: 'SearchResults',
    component: SearchResults,
    meta: { requiresAuth: true }
  },
  // 其他路由稍后添加
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 模拟认证守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true'
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router