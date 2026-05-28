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
            <el-icon class="text-text-muted cursor-pointer hover:text-text-primary"><Setting /></el-icon>
          </el-tooltip>
        </div>
      </div>

      <!-- 人格提示条 -->
      <div class="px-6 py-3 bg-gradient-to-r from-primary/10 to-accent-purple/10 border-b border-dark-border">
        <div class="flex items-center gap-2 text-sm">
          <el-icon class="text-primary"><InfoFilled /></el-icon>
          <span class="text-text-secondary">当前AI已根据你的</span>
          <el-tag size="small" effect="dark" class="bg-primary/30 border-primary/50">理解驱动型</el-tag>
          <span class="text-text-secondary">人格进行个性化调整</span>
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
            <el-avatar v-else :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          </div>

          <!-- 消息内容 -->
          <div class="max-w-[70%]" :class="message.isUser ? 'text-right' : ''">
            <div class="flex items-center gap-2 mb-1" :class="message.isUser ? 'flex-row-reverse' : ''">
              <span class="text-sm font-medium" :class="message.isUser ? 'text-text-primary' : 'text-primary'">
                {{ message.isUser ? userStore.userInfo.username : 'AI学习助手' }}
              </span>
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
                  <el-button type="primary" size="small" plain>查看</el-button>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div v-if="!message.isUser" class="flex items-center gap-2 mt-2">
              <el-button link size="small" class="text-text-muted hover:text-primary" @click="copyMessage(message.content)">
                <el-icon class="mr-1"><CopyDocument /></el-icon>复制
              </el-button>
              <el-button link size="small" class="text-text-muted hover:text-primary">
                <el-icon class="mr-1"><RefreshRight /></el-icon>重新生成
              </el-button>
              <el-button link size="small" class="text-text-muted hover:text-primary">
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
              <el-icon class="text-xl text-text-muted hover:text-primary cursor-pointer p-2"><Microphone /></el-icon>
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
import { ref, nextTick, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
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

const userStore = useUserStore()
const messageContainer = ref<HTMLElement>()
const inputMessage = ref('')
const isLoading = ref(false)
const currentChatId = ref(1)

const chatHistory = ref([
  { id: 1, title: '定积分应用问题', time: '今天 20:30' },
  { id: 2, title: '矩阵运算技巧', time: '昨天 18:15' },
  { id: 3, title: '英语阅读理解方法', time: '昨天 14:20' },
  { id: 4, title: '信号与系统基础', time: '3天前' },
])

const messages = ref([
  {
    isUser: false,
    type: 'text',
    content: '你好！我是你的AI学习助手。我已经了解了你的学习人格类型是"理解驱动型"，我会根据你的特点，用图像化讲解和简短句子来帮助你学习。有什么我可以帮你的吗？',
    time: '20:28',
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
    content: '当然可以！这道题的关键在于将图形分割为两个部分分别求面积，然后相加。\n\n让我用一个简单的类比来解释：\n\n想象你要计算一个不规则花园的面积。你可以把它分成两个矩形，分别算出每个矩形的面积，然后加起来就是总面积。\n\n对于这道题：\n1. 首先画出函数图像，找到交点\n2. 将区域分成上下两部分\n3. 分别计算两个定积分\n4. 将结果相加',
    time: '20:31',
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

const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  messages.value.push({
    isUser: true,
    type: 'text',
    content: inputMessage.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })

  const userQuestion = inputMessage.value
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  setTimeout(() => {
    isLoading.value = false
    messages.value.push({
      isUser: false,
      type: 'text',
      content: `好的，我来为你详细解答"${userQuestion}"这个问题。\n\n基于你的"理解驱动型"学习人格，我会用图像化的方式来解释：\n\n想象一下...\n\n（这里会根据具体问题生成个性化的解释，使用简短句子和图像类比）`,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
    scrollToBottom()
  }, 1500)
}

const sendQuickMessage = (text: string) => {
  inputMessage.value = text
  sendMessage()
}

const createNewChat = () => {
  const newId = chatHistory.value.length + 1
  chatHistory.value.unshift({
    id: newId,
    title: '新对话',
    time: '刚刚',
  })
  currentChatId.value = newId
  messages.value = [{
    isUser: false,
    type: 'text',
    content: '你好！我是你的AI学习助手。我已经了解了你的学习人格类型是"理解驱动型"，我会根据你的特点，用图像化讲解和简短句子来帮助你学习。有什么我可以帮你的吗？',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  }]
}

const selectChat = (id: number) => {
  currentChatId.value = id
}

const deleteChat = (id: number) => {
  chatHistory.value = chatHistory.value.filter(chat => chat.id !== id)
}

const clearChat = () => {
  messages.value = []
}

const copyMessage = (content: string) => {
  navigator.clipboard.writeText(content)
}

onMounted(() => {
  scrollToBottom()
})
</script>
