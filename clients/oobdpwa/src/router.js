import Vue from 'vue'
import VueRouter from 'vue-router'
import Main from '@/views/Main'
import Start from '@/views/Start'
import Log from '@/views/Log'
Vue.use(VueRouter)

/*
const router = new VueRouter({
  scrollBehavior: () => ({ y: 0 }),
  routes: [
    {
      path: '/:filter',
      name: 'Main',
      component: Main,
      props: true
    }
  ]
})

router.beforeEach((to, from, next) => {
  if (['all', 'active', 'completed'].some(record => record === to.params.filter)) {
    next()
  } else {
    next('/all')
  }

[
  {
    path: '/',
    name: 'Main',
    component: Main
  },
  {
    path: '/Log/:uuid',
    name: 'Log',
    component: Log
  },
  {
    path: '/start',
    name: 'Start',
    component: Start
  },
  {
    path: '*',
    redirect: '/Main'
  }
]

})
*/

const routes = [
  {
    path: '/start',
    name: 'Start',
    component: Start
  },
  {
    path: '/main',
    name: 'Main',
    component: Main
  },
  {
    path: '/log',
    name: 'Log',
    component: Log
  },
  {
    path: '*',
    redirect: '/start'
  }
]

const router = new VueRouter({
  routes
})

export default router
