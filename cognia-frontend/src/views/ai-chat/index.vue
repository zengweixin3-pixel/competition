<template>
  <div class="h-[calc(100vh-140px)] flex gap-6">
    <!-- 左侧：历史会话 -->
    <div class="w-72 card-gradient rounded-2xl flex flex-col">
      <div class="p-4 border-b border-dark-border">
        <el-button type="primary" class="w-full" size="large" @click="createNewChat">
          <el-icon class="mr-2"><Plus /></el-icon>新建对话
        </el-button>
      </div>
      <div class="flex-1 overflow-y-auto p-4 space-y-2">
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          class="p-3 rounded-xl cursor-pointer transition-all duration-300 group"
          :class="currentChatId === chat.id ? 'bg-primary/20 border border-primary/30' : 'hover:bg-dark-border/50 border border-transparent'"
          @click="selectChat(chat.id)"
        >
          <div class="flex items-center gap-3">
            <el-icon class="text-text-muted" :class="currentChatId === chat.id ? 'text-primary' : ''"><ChatRound /></el-icon>
            <div class="flex-1 min-w-0">
              <div class="text-sm text-text-primary truncate">{{ chat.title }}</div>
              <div class="text-xs text-text-muted">{{ chat.time }}</div>
            </div>
            <el-icon class="text-text-muted opacity-0 group-hover:opacity-100 cursor-pointer hover:text-rose-400" @click.stop="deleteChat(chat.id)"><Delete /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：聊天区域 -->
    <div class="flex-1 card-gradient rounded-2xl flex flex-col overflow-hidden">
      <!-- 聊天头部 -->
      <div class="h-16 border-b border-dark-border flex items-center justify-between px-6">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-gradient-primary flex items-center justify-center">
            <el-icon class="text-white"><ChatDotRound /></el-icon>
          </div>
          <div>
            <div class="font-bold text-text-primary">AI学习助手</div>
            <div class="text-xs text-text-muted flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              在线
            </div>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <el-tooltip content="清空对话">
            <el-icon class="text-text-muted cursor-pointer hover:text-text-primary" @click="clearChat"><Delete /></el-icon>
          </el-tooltip>
          <el-tooltip content="设置">
            <el-icon class="text-text-muted cursor-pointer hover:text-text-primary" @click="ElMessage.info('AI助手设置开发中...')"><Setting /></el-icon>
          </el-tooltip>
        </div>
      </div>

      <!-- 人格提示条 + Agent选择 -->
      <div class="px-6 py-3 bg-gradient-to-r from-primary/10 to-accent-purple/10 border-b border-dark-border">
        <div class="flex items-center gap-2 text-sm flex-wrap">
          <el-icon class="text-primary"><InfoFilled /></el-icon>
          <span class="text-text-secondary">当前</span>
          <el-tag size="small" effect="dark" class="bg-primary/30 border-primary/50">{{ userStore.learningDNA.type || '默认' }}</el-tag>
          <span class="text-text-secondary">路由</span>
          <el-select v-model="routingMode" size="small" style="width: 128px" @change="onRoutingModeChange">
            <el-option label="自动路由" value="auto" />
            <el-option label="手动指定" value="manual" />
          </el-select>
          <span class="text-text-secondary">Agent</span>
          <el-select v-model="currentAgent" size="small" style="width: 156px" :disabled="routingMode === 'auto'" @change="onAgentChange">
            <el-option v-for="a in agents" :key="a.value" :label="a.label" :value="a.value">
              <span>{{ a.icon }} {{ a.label }}</span>
            </el-option>
          </el-select>
          <span class="text-text-muted text-xs ml-auto">{{ routingHint }}</span>
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="messageContainer" class="flex-1 overflow-y-auto p-6 space-y-6">
        <div v-for="(message, index) in messages" :key="index" class="flex gap-4" :class="message.isUser ? 'flex-row-reverse' : ''">
          <!-- 头像 -->
          <div class="flex-shrink-0">
            <div v-if="!message.isUser" class="w-10 h-10 rounded-full bg-gradient-primary flex items-center justify-center">
              <el-icon class="text-white"><ChatDotRound /></el-icon>
            </div>
            <el-avatar v-else :size="40" :src="userStore.avatarUrl" />
          </div>

          <!-- 消息内容 -->
          <div class="max-w-[70%]" :class="message.isUser ? 'text-right' : ''">
            <div class="flex items-center gap-2 mb-1" :class="message.isUser ? 'flex-row-reverse' : ''">
              <span class="text-sm font-medium" :class="message.isUser ? 'text-text-primary' : 'text-primary'">
                {{ message.isUser ? userStore.userInfo.username : 'AI学习助手' }}
              </span>
              <el-tag v-if="!message.isUser && message.agentLabel" size="small" effect="plain" type="primary">
                {{ message.agentLabel }}
              </el-tag>
              <span class="text-xs text-text-muted">{{ message.time }}</span>
            </div>

            <!-- 文本消息 -->
            <div
              v-if="message.type === 'text'"
              class="inline-block px-4 py-3 rounded-2xl text-left"
              :class="message.isUser ? 'bg-primary text-white' : 'bg-dark-border text-text-primary'"
            >
              <div class="text-sm leading-relaxed whitespace-pre-wrap">{{ message.content }}</div>
            </div>

            <!-- 图片消息 -->
            <div v-else-if="message.type === 'image'" class="rounded-2xl overflow-hidden max-w-sm">
              <img :src="message.content" class="w-full" />
            </div>

            <!-- 推荐卡片 -->
            <div v-else-if="message.type === 'cards'" class="space-y-3">
              <div
                v-for="(card, idx) in message.cards"
                :key="idx"
                class="bg-dark-bg/50 rounded-xl p-4 border border-dark-border hover:border-primary/50 transition-all cursor-pointer"
              >
                <div class="flex items-start gap-3">
                  <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="card.iconBg">
                    <el-icon :class="card.iconColor"><component :is="card.icon" /></el-icon>
                  </div>
                  <div class="flex-1">
                    <div class="font-medium text-text-primary mb-1">{{ card.title }}</div>
                    <p class="text-sm text-text-secondary">{{ card.desc }}</p>
                  </div>
                  <el-button type="primary" size="small" plain @click="searchVideo(card.title)">查看</el-button>
                </div>
              </div>
            </div>

            <p v-if="!message.isUser && message.routeReason" class="mt-2 text-xs leading-5 text-text-muted">
              {{ message.routeReason }}
            </p>

            <!-- 操作按钮 -->
            <div v-if="!message.isUser" class="flex items-center gap-2 mt-2">
              <el-button link size="small" class="text-text-muted hover:text-primary" @click="copyMessage(message.content || '')">
                <el-icon class="mr-1"><CopyDocument /></el-icon>复制
              </el-button>
              <el-button link size="small" class="text-text-muted hover:text-primary" @click="regenerateMessage()">
                <el-icon class="mr-1"><RefreshRight /></el-icon>重新生成
              </el-button>
              <el-button link size="small" class="text-text-muted hover:text-primary" @click="starMessage">
                <el-icon class="mr-1"><Star /></el-icon>收藏
              </el-button>
            </div>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="isLoading" class="flex gap-4">
          <div class="w-10 h-10 rounded-full bg-gradient-primary flex items-center justify-center">
            <el-icon class="text-white"><ChatDotRound /></el-icon>
          </div>
          <div class="bg-dark-border rounded-2xl px-4 py-3">
            <div class="flex items-center gap-2">
              <div class="w-2 h-2 rounded-full bg-primary animate-bounce"></div>
              <div class="w-2 h-2 rounded-full bg-primary animate-bounce" style="animation-delay: 0.1s"></div>
              <div class="w-2 h-2 rounded-full bg-primary animate-bounce" style="animation-delay: 0.2s"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="px-6 py-3 border-t border-dark-border">
        <div class="flex items-center gap-3 overflow-x-auto scrollbar-hide">
          <span class="text-sm text-text-muted whitespace-nowrap">快捷提问：</span>
          <el-tag
            v-for="(quick, idx) in quickQuestions"
            :key="idx"
            effect="dark"
            class="cursor-pointer hover:bg-primary/30 whitespace-nowrap"
            @click="sendQuickMessage(quick)"
          >
            {{ quick }}
          </el-tag>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="p-6 border-t border-dark-border">
        <div class="flex items-end gap-3 bg-dark-bg/50 rounded-2xl p-3 border border-dark-border focus-within:border-primary/50 transition-all">
          <el-upload action="#" :auto-upload="false" :show-file-list="false" class="flex-shrink-0">
            <el-icon class="text-xl text-text-muted hover:text-primary cursor-pointer p-2"><Picture /></el-icon>
          </el-upload>
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            placeholder="输入你的学习问题，AI会根据你的学习人格个性化回答..."
            class="flex-1"
            resize="none"
            @keyup.enter.prevent="sendMessage"
          />
          <div class="flex items-center gap-2">
            <el-tooltip content="语音输入">
              <el-icon class="text-xl text-text-muted hover:text-primary cursor-pointer p-2" @click="ElMessage.info('语音输入功能开发中...')"><Microphone /></el-icon>
            </el-tooltip>
            <el-button type="primary" :icon="Promotion" :disabled="!inputMessage.trim() || isLoading" @click="sendMessage">
              发送
            </el-button>
          </div>
        </div>
        <div class="text-center mt-2">
          <span class="text-xs text-text-muted">AI生成的内容仅供参考，请结合教材和课堂内容学习</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api'
import type { ChatResponse } from '@/types'
import {
  Plus,
  ChatRound,
  Delete,
  ChatDotRound,
  Setting,
  InfoFilled,
  CopyDocument,
  RefreshRight,
  Star,
  Picture,
  Microphone,
  Promotion,
  Document,
  VideoPlay,
  Collection,
} from '@element-plus/icons-vue'

type CardItem = {
  title: string
  desc: string
  icon: string
  iconBg: string
  iconColor: string
}

type MessageItem = {
  isUser: boolean
  type: 'text' | 'image' | 'cards'
  content?: string
  time: string
  cards?: CardItem[]
  agent?: string
  agentLabel?: string
  routeReason?: string
}

const userStore = useUserStore()
const messageContainer = ref<HTMLElement>()
const inputMessage = ref('')
const isLoading = ref(false)
const currentChatId = ref(1)
const routingMode = ref<'auto' | 'manual'>('auto')

const agents = [
  { label: '学习教练', value: 'coach', icon: '🎓', desc: '讲解知识、解答问题' },
  { label: '错题分析师', value: 'analyst', icon: '🔍', desc: '分析错因、给出改进方案' },
  { label: '情绪伙伴', value: 'companion', icon: '💜', desc: '倾听感受、调节情绪' },
  { label: '计划规划师', value: 'planner', icon: '📋', desc: '制定个性化学习计划' },
]
const currentAgent = ref('coach')

const activeAgent = computed(() => agents.find(item => item.value === currentAgent.value) || agents[0])
const routingHint = computed(() => {
  if (routingMode.value === 'auto') {
    return '系统会根据你的问题自动分配最合适的 Agent。'
  }
  return activeAgent.value.desc
})

const onRoutingModeChange = () => {
  ElMessage.success(routingMode.value === 'auto' ? '已切换为自动路由模式' : '已切换为手动指定模式')
}

const onAgentChange = () => {
  if (routingMode.value === 'manual') {
    ElMessage.success(`已指定由「${activeAgent.value.label}」处理`)
  }
}

const chatHistory = ref([
  { id: 1, title: '定积分应用问题', time: '今天 20:30' },
  { id: 2, title: '矩阵运算技巧', time: '昨天 18:15' },
  { id: 3, title: '英语阅读理解方法', time: '昨天 14:20' },
  { id: 4, title: '信号与系统基础', time: '3天前' },
])

const messages = ref<MessageItem[]>([
  {
    isUser: false,
    type: 'text',
    content: '你好，我是你的多 Agent 学习助手。你可以直接提问，我会根据问题自动分配给学习教练、错题分析师、情绪伙伴或学习规划师。',
    time: '20:28',
    agent: 'coach',
    agentLabel: '学习教练',
    routeReason: '这是对话初始化说明，由学习教练先接手欢迎你进入多 Agent 模式。',
  },
  {
    isUser: true,
    type: 'text',
    content: '这个定积分题我还是不太理解，可以再讲一下吗？',
    time: '20:30',
  },
  {
    isUser: false,
    type: 'text',
    content: '当然可以。这类问题更适合由学习教练处理，我会先帮你拆思路，再解释每一步为什么这样做。\n\n这道题的关键是先画图，再确认积分区间和上下函数，最后再按面积含义去理解定积分。',
    time: '20:31',
    agent: 'coach',
    agentLabel: '学习教练',
    routeReason: '检测到你在询问知识点理解与解题讲解，已交给学习教练。',
  },
  {
    isUser: false,
    type: 'cards',
    cards: [
      { title: '定积分求面积详解', desc: '包含3个例题和详细图解', icon: 'Document', iconBg: 'bg-primary/20', iconColor: 'text-primary' },
      { title: '相关视频讲解', desc: '15分钟快速掌握核心方法', icon: 'VideoPlay', iconBg: 'bg-accent-purple/20', iconColor: 'text-accent-purple' },
      { title: '练习题推荐', desc: '5道精选练习题巩固知识', icon: 'Collection', iconBg: 'bg-emerald-500/20', iconColor: 'text-emerald-400' },
    ],
    time: '20:31',
    agent: 'coach',
    agentLabel: '学习教练',
  },
])

const quickQuestions = ref([
  '帮我解释这个公式',
  '推荐一些练习题',
  '我哪里理解错了？',
  '用更简单的方式讲',
  '这个知识点的应用场景',
])

const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

const buildRequestPayload = (message: string) => {
  const forcedAgent = routingMode.value === 'manual' ? currentAgent.value : 'auto'
  return {
    message,
    userDNA: userStore.learningDNA.type || userStore.userInfo.learningType,
    emotion: userStore.userInfo.emotionState,
    context: '',
    source: 'ai-chat',
    forcedAgent,
    agent: forcedAgent,
  }
}

const appendAssistantMessage = (result: ChatResponse, fallback: string) => {
  messages.value.push({
    isUser: false,
    type: 'text',
    content: result.response || fallback,
    time: getCurrentTimeLabel(),
    agent: result.agent,
    agentLabel: result.agentLabel || 'AI学习助手',
    routeReason: result.routeReason || '',
  })
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || isLoading.value) return

  messages.value.push({
    isUser: true,
    type: 'text',
    content,
    time: getCurrentTimeLabel(),
  })

  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const result = await aiApi.chat(buildRequestPayload(content))
    appendAssistantMessage(result, '抱歉，AI暂时无法回复，请稍后再试。')
  } catch (error) {
    const message = error instanceof Error ? error.message : 'AI 服务异常，请稍后重试'
    ElMessage.error(message)
    appendAssistantMessage(
      {
        response: 'AI 服务暂时不可用，请稍后再试。',
        agentLabel: '系统提示',
        routeReason: '当前请求未能成功进入多 Agent 流程。',
      },
      'AI 服务暂时不可用，请稍后再试。',
    )
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const sendQuickMessage = (text: string) => {
  inputMessage.value = text
  sendMessage()
}

const buildWelcomeMessage = (): MessageItem => ({
  isUser: false,
  type: 'text',
  content: '你好，我是你的多 Agent 学习助手。默认会自动路由问题；如果你已经明确知道要找谁，也可以切到手动指定模式。',
  time: getCurrentTimeLabel(),
  agent: 'coach',
  agentLabel: '学习教练',
  routeReason: '这是新会话的默认欢迎消息。',
})

const createNewChat = () => {
  const newId = chatHistory.value.length + 1
  chatHistory.value.unshift({
    id: newId,
    title: '新对话',
    time: '刚刚',
  })
  currentChatId.value = newId
  messages.value = [buildWelcomeMessage()]
}

const selectChat = (id: number) => {
  currentChatId.value = id
}

const deleteChat = (id: number) => {
  chatHistory.value = chatHistory.value.filter(chat => chat.id !== id)
}

const clearChat = () => {
  messages.value = [buildWelcomeMessage()]
}

const copyMessage = (content: string) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

const regenerateMessage = async () => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    const result = await aiApi.chat(buildRequestPayload('请换一个角度重新解释上一个问题'))
    appendAssistantMessage(result, '抱歉，AI暂时无法回复。')
    scrollToBottom()
  } catch (error) {
    const message = error instanceof Error ? error.message : '重新生成失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    isLoading.value = false
  }
}

const searchVideo = (title: string) => {
  window.open('https://www.bilibili.com/search?keyword=' + encodeURIComponent(title), '_blank')
}

const starMessage = () => {
  ElMessage.success('已收藏到知识库')
}

const getCurrentTimeLabel = () => {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  scrollToBottom()
})
</script>
