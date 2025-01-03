import { createRouter, createWebHistory } from 'vue-router'
import PkIndexView from '@/views/pk/PkIndexView.vue'
import NotFoundIndexView from '@/views/error/NotFoundIndexView.vue'
import RankListIndexView from '@/views/ranklist/RankListIndexView.vue'
import RecordIndexView from '@/views/record/RecordIndexView.vue'
import UserBotsIndexView from '@/views/user/bots/UserBotsIndexView.vue'
import UserAccountLoginView from '@/views/user/account/UserAccountLoginView.vue'
import UserAccountRegisterView from '@/views/user/account/UserAccountRegisterView.vue'
import FriendsIndexView from '@/views/friends/FriendsIndexView.vue'
import store from '@/store/index'

const routes = [
  {
    path: "/",
    redirect: "/pk/",
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/pk/",
    component: PkIndexView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/record/",
    component: RecordIndexView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/ranklist/",
    component: RankListIndexView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/friends/",
    component: FriendsIndexView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/user/bots/",
    component: UserBotsIndexView,
    meta: {
      requestAuth: true
    }
  },
  {
    path: "/user/account/login/",
    component: UserAccountLoginView,
    meta: {
      requestAuth: false
    }
  },
  {
    path: "/user/account/register",
    component: UserAccountRegisterView,
    meta: {
      requestAuth: false
    }
  },
  {
    path: "/404/",
    component: NotFoundIndexView,
    meta: {
      requestAuth: false
    }
  },
  {
    path: "/:catchAll(.*)",
    redirect: "/404/"
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {

  const jwt_token = localStorage.getItem("jwt_token");

  if (jwt_token) {
    store.commit("updateToken", jwt_token);
    store.dispatch("getInfo", {
      success() {
        next()
      },
    })
  } else {
    if(to.meta.requestAuth){
      //console.log(store.state.is_login)
      next("/user/account/login/") 
    } else {
      next()
    }
  }
})

export default router
