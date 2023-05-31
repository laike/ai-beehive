package cn.beehive.cell.openai.module.chat.storage;

import cn.beehive.base.domain.entity.RoomOpenAiChatMsgDO;
import cn.beehive.base.enums.ChatMessageStatusEnum;
import cn.beehive.base.enums.MessageTypeEnum;
import cn.beehive.cell.openai.service.RoomOpenAiChatMsgService;
import cn.hutool.core.util.StrUtil;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author hncboy
 * @date 2023-3-25
 * ApiKey 数据库数据存储
 */
@Component
public class ApiKeyDatabaseDataStorage extends AbstractDatabaseDataStorage {

    @Resource
    private RoomOpenAiChatMsgService roomOpenAiChatMsgService;

    @Override
    public void onFirstMessage(RoomOpenAiChatMessageStorage chatMessageStorage) {
        RoomOpenAiChatMsgDO questionMessage = (RoomOpenAiChatMsgDO) chatMessageStorage.getQuestionMessageDO();
        RoomOpenAiChatMsgDO answerMessage = new RoomOpenAiChatMsgDO();
        answerMessage.setUserId(questionMessage.getUserId());
        answerMessage.setRoomId(questionMessage.getRoomId());
        answerMessage.setIp(questionMessage.getIp());
        answerMessage.setParentQuestionMessageId(questionMessage.getId());
        answerMessage.setMessageType(MessageTypeEnum.ANSWER);
        answerMessage.setModelName(questionMessage.getModelName());
        answerMessage.setApiKey(questionMessage.getApiKey());
        answerMessage.setContent(chatMessageStorage.getReceivedMessage());
        answerMessage.setOriginalData(chatMessageStorage.getOriginalResponseData());
        answerMessage.setPromptTokens(questionMessage.getPromptTokens());
        answerMessage.setStatus(ChatMessageStatusEnum.PART_SUCCESS);
        // 保存回答消息
        roomOpenAiChatMsgService.save(answerMessage);

        // 设置回答消息
        chatMessageStorage.setAnswerMessageDO(answerMessage);

        // 更新问题消息状态为部分成功
        questionMessage.setStatus(ChatMessageStatusEnum.PART_SUCCESS);
        roomOpenAiChatMsgService.updateById(questionMessage);
    }

    @Override
    void onLastMessage(RoomOpenAiChatMessageStorage chatMessageStorage) {
        RoomOpenAiChatMsgDO questionMessage = (RoomOpenAiChatMsgDO) chatMessageStorage.getQuestionMessageDO();
        RoomOpenAiChatMsgDO answerMessage = (RoomOpenAiChatMsgDO) chatMessageStorage.getAnswerMessageDO();

        // 成功状态
        questionMessage.setStatus(ChatMessageStatusEnum.COMPLETE_SUCCESS);
        answerMessage.setStatus(ChatMessageStatusEnum.COMPLETE_SUCCESS);

        // 原始响应数据
        answerMessage.setOriginalData(chatMessageStorage.getOriginalResponseData());
        answerMessage.setContent(chatMessageStorage.getReceivedMessage());

        // 填充使用 token
        populateMessageUsageToken(chatMessageStorage);

        // 更新消息
        roomOpenAiChatMsgService.updateById(questionMessage);
        roomOpenAiChatMsgService.updateById(answerMessage);
    }

    @Override
    void onErrorMessage(RoomOpenAiChatMessageStorage chatMessageStorage) {
        // 消息流条数大于 0 表示部分成功
        ChatMessageStatusEnum chatMessageStatusEnum = chatMessageStorage.getCurrentStreamMessageCount() > 0 ? ChatMessageStatusEnum.PART_SUCCESS : ChatMessageStatusEnum.ERROR;

        // 填充问题消息记录
        RoomOpenAiChatMsgDO questionMessage = (RoomOpenAiChatMsgDO) chatMessageStorage.getQuestionMessageDO();
        questionMessage.setStatus(chatMessageStatusEnum);
        // 错误响应数据
        questionMessage.setResponseErrorData(chatMessageStorage.getErrorResponseData());

        // 填充使用 token
        populateMessageUsageToken(chatMessageStorage);

        // 还没收到回复就断了，跳过回答消息记录更新
        if (chatMessageStatusEnum != ChatMessageStatusEnum.ERROR) {
            // 填充问题消息记录
            RoomOpenAiChatMsgDO answerMessage = (RoomOpenAiChatMsgDO) chatMessageStorage.getAnswerMessageDO();
            answerMessage.setStatus(chatMessageStatusEnum);
            // 原始响应数据
            answerMessage.setOriginalData(chatMessageStorage.getOriginalResponseData());
            // 错误响应数据
            answerMessage.setResponseErrorData(chatMessageStorage.getErrorResponseData());
            answerMessage.setContent(chatMessageStorage.getReceivedMessage());
            // 更新错误的回答消息记录
            roomOpenAiChatMsgService.updateById(answerMessage);
        }

        // 更新错误的问题消息记录
        roomOpenAiChatMsgService.updateById(questionMessage);
    }

    /**
     * 填充消息使用 Token 数量
     *
     * @param chatMessageStorage 聊天消息数据存储
     */
    private void populateMessageUsageToken(RoomOpenAiChatMessageStorage chatMessageStorage) {
        Object answerMessageObj = chatMessageStorage.getAnswerMessageDO();
        if (Objects.isNull(answerMessageObj)) {
            return;
        }

        // 获取模型
        RoomOpenAiChatMsgDO questionMessage = (RoomOpenAiChatMsgDO) chatMessageStorage.getQuestionMessageDO();
        String modelName = questionMessage.getModelName();

        // 获取回答消耗的 tokens
        RoomOpenAiChatMsgDO answerMessage = (RoomOpenAiChatMsgDO) answerMessageObj;
        String answerContent = answerMessage.getContent();
        int completionTokens = StrUtil.isEmpty(answerContent) ? 0 : TikTokensUtil.tokens(modelName, answerContent);

        // 填充使用情况
        int totalTokens = questionMessage.getPromptTokens() + completionTokens;
        answerMessage.setPromptTokens(questionMessage.getPromptTokens());
        answerMessage.setCompletionTokens(completionTokens);
        answerMessage.setTotalTokens(totalTokens);

        questionMessage.setCompletionTokens(completionTokens);
        questionMessage.setTotalTokens(totalTokens);
    }
}
