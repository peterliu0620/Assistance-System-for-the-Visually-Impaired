<template>
	<view class="page">
		<view class="card">
			<text class="title">视障智能视觉辅助</text>
			<text class="desc">说出“这是什么？”，系统将拍照识别并语音播报</text>

			<button class="btn primary" @click="startVoiceTrigger" :disabled="loading">
				{{ loading ? '识别中...' : '语音触发识别' }}
			</button>
			<button class="btn" @click="scanByDefaultCommand" :disabled="loading">直接拍照识别</button>

			<view class="result" v-if="result">
				<text class="result-title">结构化播报结果</text>
				<text class="line">触发指令：{{ result.triggerCommand }}</text>
				<text class="line">场景：{{ result.scene }}</text>
				<text class="line">说明：{{ result.positionSummary }}</text>
				<text class="line">播报：{{ result.narration }}</text>
			</view>

			<view class="debug">
				<view class="debug-head">
					<text class="debug-title">调试信息</text>
					<button class="tiny" @click="clearDebugLogs">清空</button>
				</view>
				<text class="debug-line">API: {{ apiBase }}</text>
				<text class="debug-line">状态: {{ debug.status }}</text>
				<text class="debug-line">最后指令: {{ debug.lastCommand || '-' }}</text>
				<text class="debug-line">最后图片: {{ debug.lastImagePath || '-' }}</text>
				<text class="debug-line">最后响应码: {{ debug.lastStatusCode || '-' }}</text>
				<text class="debug-line">最后错误: {{ debug.lastError || '-' }}</text>
				<text class="debug-log" v-for="(log, idx) in debug.logs" :key="idx">{{ log }}</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				loading: false,
				result: null,
				apiBase: 'http://10.135.102.177:8080',
				audioPlayer: null,
				debug: {
					status: 'idle',
					lastCommand: '',
					lastImagePath: '',
					lastStatusCode: '',
					lastError: '',
					logs: []
				}
			}
		},
		onLoad() {
			this.audioPlayer = uni.createInnerAudioContext()
			this.audioPlayer.autoplay = true
			this.audioPlayer.onPlay(() => {
				this.debug.status = 'spoken-cloud-playing'
				this.pushDebugLog('云端音频开始播放')
			})
			this.audioPlayer.onEnded(() => {
				this.debug.status = 'spoken-cloud-ended'
				this.pushDebugLog('云端音频播放结束')
			})
			this.audioPlayer.onError((err) => {
				this.debug.status = 'spoken-cloud-failed'
				this.setDebugError(`云端音频播放失败: ${JSON.stringify(err)}`)
			})
		},
		onUnload() {
			if (this.audioPlayer) {
				this.audioPlayer.destroy()
				this.audioPlayer = null
			}
		},
		methods: {
			pushDebugLog(message) {
				const now = new Date()
				const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
				const line = `[${time}] ${message}`
				this.debug.logs.unshift(line)
				console.log(`[VISION-DEBUG] ${line}`)
				if (this.debug.logs.length > 10) {
					this.debug.logs.pop()
				}
			},
			setDebugError(message) {
				this.debug.lastError = message
				console.error(`[VISION-DEBUG][ERROR] ${message}`)
				this.pushDebugLog(`ERROR: ${message}`)
			},
			clearDebugLogs() {
				this.debug.logs = []
				this.debug.lastError = ''
				this.pushDebugLog('已清空调试日志')
			},
			startVoiceTrigger() {
				if (this.loading) {
					return
				}
				const command = '这是什么？'
				this.debug.status = 'voice-triggered'
				this.debug.lastCommand = command
				this.pushDebugLog('语音触发识别：先播报提示，再自动拍照')
				this.playGuideThenCapture(command)
			},
			playGuideThenCapture(command) {
				const guide = '即将开始识别，请将目标放入画面后拍照。'
				let spoken = false
				if (typeof plus !== 'undefined') {
					if (plus.speech && typeof plus.speech.speak === 'function') {
						plus.speech.speak(guide)
						spoken = true
					} else if (plus.tts && typeof plus.tts.speak === 'function') {
						plus.tts.speak(guide)
						spoken = true
					}
				} else if (typeof window !== 'undefined' && window.speechSynthesis) {
					const utterance = new SpeechSynthesisUtterance(guide)
					utterance.lang = 'zh-CN'
					window.speechSynthesis.speak(utterance)
					spoken = true
				}

				if (!spoken) {
					uni.showToast({ title: '即将打开相机，请拍照', icon: 'none' })
					this.pushDebugLog('本地提示音不可用，使用文字提示')
				} else {
					this.pushDebugLog('已播报拍照引导语')
				}

				setTimeout(() => {
					this.pickAndAnalyzeImage(command)
				}, 600)
			},
			handleVoiceCommand(command) {
				const normalized = (command || '').trim()
				if (!normalized) {
					this.setDebugError('未识别到有效语音指令')
					uni.showToast({ title: '未识别到指令', icon: 'none' })
					return
				}
				this.debug.lastCommand = normalized
				this.debug.status = 'command-ready'
				this.pushDebugLog(`收到指令: ${normalized}`)

				if (normalized.indexOf('什么') === -1 && normalized.indexOf('这') === -1) {
					uni.showToast({ title: `当前指令：${normalized}` , icon: 'none' })
				}

				this.pickAndAnalyzeImage(normalized)
			},
			scanByDefaultCommand() {
				this.pickAndAnalyzeImage('这是什么？')
			},
			pickAndAnalyzeImage(command) {
				this.loading = true
				this.debug.status = 'camera-opening'
				this.pushDebugLog('打开相机拍照')
				uni.chooseImage({
					count: 1,
					sourceType: ['camera'],
					success: (chooseRes) => {
						const imagePath = chooseRes.tempFilePaths[0]
						this.debug.lastImagePath = imagePath || ''
						this.debug.status = 'image-picked'
						this.pushDebugLog(`拍照成功: ${imagePath}`)
						this.uploadForAnalyze(imagePath, command)
					},
					fail: () => {
						this.loading = false
						this.debug.status = 'camera-failed'
						this.setDebugError('未获取到照片')
						uni.showToast({ title: '未获取到照片', icon: 'none' })
					}
				})
			},
			uploadForAnalyze(imagePath, command) {
				this.debug.status = 'uploading'
				this.pushDebugLog(`开始上传: ${this.apiBase}/api/vision/analyze`)
				uni.uploadFile({
					url: `${this.apiBase}/api/vision/analyze`,
					filePath: imagePath,
					name: 'image',
					formData: {
						command
					},
					success: (uploadRes) => {
						this.loading = false
						this.debug.lastStatusCode = uploadRes.statusCode
						this.pushDebugLog(`上传完成，HTTP ${uploadRes.statusCode}`)
						if (uploadRes.statusCode !== 200) {
							let detail = uploadRes.data
							try {
								const errorJson = JSON.parse(uploadRes.data || '{}')
								detail = errorJson.message || JSON.stringify(errorJson)
							} catch (e) {
								// keep raw detail
							}
							this.debug.status = 'analyze-failed'
							this.setDebugError(`识别请求失败: HTTP ${uploadRes.statusCode} ${detail || ''}`)
							uni.showToast({ title: '识别失败，请查看调试信息', icon: 'none' })
							return
						}
						let data = null
						try {
							data = JSON.parse(uploadRes.data)
						} catch (e) {
							this.debug.status = 'parse-failed'
							this.setDebugError(`返回数据解析失败: ${e && e.message ? e.message : 'unknown'}`)
							uni.showToast({ title: '返回数据解析失败', icon: 'none' })
							return
						}

						this.result = data
						this.debug.status = 'analyzed'
						this.pushDebugLog('返回解析成功，准备语音播报')
						try {
							this.speak(data.narration || '识别完成')
						} catch (e) {
							this.debug.status = 'speak-failed'
							this.setDebugError(`语音播报失败: ${e && e.message ? e.message : 'unknown'}`)
						}
					},
					fail: () => {
						this.loading = false
						this.debug.status = 'upload-failed'
						this.setDebugError('识别请求失败，请确认后端已启动')
						uni.showToast({ title: '识别请求失败，请确认后端已启动', icon: 'none' })
					}
				})
			},
			speak(text) {
				if (!text) {
					return
				}
				this.pushDebugLog(`开始播报: ${text}`)

				if (typeof plus !== 'undefined') {
					if (plus.speech && typeof plus.speech.speak === 'function') {
						plus.speech.speak(text)
						this.debug.status = 'spoken-app'
						this.pushDebugLog('使用 plus.speech.speak 完成播报')
						return
					}

					if (plus.tts && typeof plus.tts.speak === 'function') {
						plus.tts.speak(text)
						this.debug.status = 'spoken-app'
						this.pushDebugLog('使用 plus.tts.speak 完成播报')
						return
					}

					this.debug.status = 'spoken-cloud-request'
					this.pushDebugLog('当前基座不支持 App TTS，切换云端 TTS')
					this.playCloudTts(text)
					return
				}

				if (typeof window !== 'undefined' && window.speechSynthesis) {
					const utterance = new SpeechSynthesisUtterance(text)
					utterance.lang = 'zh-CN'
					window.speechSynthesis.speak(utterance)
					this.debug.status = 'spoken-web'
					this.pushDebugLog('使用 Web Speech Synthesis 完成播报')
					return
				}

				this.debug.status = 'spoken-cloud-request'
				this.pushDebugLog('当前环境无本地播报能力，切换云端 TTS')
				this.playCloudTts(text)
			},
			playCloudTts(text) {
				uni.request({
					url: `${this.apiBase}/api/voice/tts`,
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data: {
						text
					},
					success: (res) => {
						if (res.statusCode !== 200 || !res.data || !res.data.audioUrl) {
							const detail = res.data && res.data.message ? res.data.message : JSON.stringify(res.data || {})
							this.setDebugError(`云端TTS请求失败: HTTP ${res.statusCode} ${detail}`)
							return
						}
						const audioUrl = res.data.audioUrl
						this.pushDebugLog(`云端TTS成功，音频地址: ${audioUrl}`)
						this.playFromUrl(audioUrl)
					},
					fail: (err) => {
						this.setDebugError(`云端TTS请求异常: ${JSON.stringify(err)}`)
					}
				})
			},
			playFromUrl(url) {
				if (!this.audioPlayer) {
					this.audioPlayer = uni.createInnerAudioContext()
					this.audioPlayer.autoplay = true
				}
				this.debug.status = 'spoken-cloud-ready'
				this.pushDebugLog('准备播放云端音频')
				this.audioPlayer.src = url
				this.audioPlayer.play()
			}
		}
	}
</script>

<style>
	.page {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #0e1116;
		padding: 32rpx;
	}

	.card {
		width: 100%;
		max-width: 680rpx;
		background: #171b22;
		border: 2rpx solid #f8d76a;
		border-radius: 20rpx;
		padding: 28rpx;
		box-sizing: border-box;
	}

	.title {
		display: block;
		font-size: 44rpx;
		color: #f8d76a;
		font-weight: bold;
		margin-bottom: 16rpx;
	}

	.desc {
		display: block;
		font-size: 28rpx;
		color: #e8e8e8;
		line-height: 1.6;
		margin-bottom: 24rpx;
	}

	.btn {
		margin-bottom: 20rpx;
		background: #2a2f3a;
		color: #ffffff;
	}

	.primary {
		background: #f8d76a;
		color: #111111;
		font-weight: bold;
	}

	.result {
		margin-top: 24rpx;
		padding: 20rpx;
		border-radius: 16rpx;
		background: #10141b;
		border: 2rpx solid #2e3644;
	}

	.result-title {
		display: block;
		font-size: 30rpx;
		color: #f8d76a;
		margin-bottom: 10rpx;
	}

	.line {
		display: block;
		font-size: 26rpx;
		color: #e5e7eb;
		line-height: 1.6;
		margin-bottom: 8rpx;
	}

	.debug {
		margin-top: 24rpx;
		padding: 20rpx;
		border-radius: 16rpx;
		background: #0a0f14;
		border: 2rpx dashed #3a4658;
	}

	.debug-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 10rpx;
	}

	.debug-title {
		font-size: 28rpx;
		color: #8bd3ff;
		font-weight: bold;
	}

	.tiny {
		font-size: 22rpx;
		padding: 0 16rpx;
		height: 52rpx;
		line-height: 52rpx;
		background: #233142;
		color: #cfe8ff;
		margin: 0;
	}

	.debug-line {
		display: block;
		font-size: 22rpx;
		color: #b8d3e8;
		line-height: 1.6;
		margin-bottom: 6rpx;
		word-break: break-all;
	}

	.debug-log {
		display: block;
		font-size: 22rpx;
		color: #93f5c2;
		line-height: 1.5;
		margin-top: 6rpx;
		word-break: break-all;
	}
</style>
