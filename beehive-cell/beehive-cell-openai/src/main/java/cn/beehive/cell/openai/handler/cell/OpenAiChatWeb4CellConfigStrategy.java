package cn.beehive.cell.openai.handler.cell;

import cn.beehive.base.enums.CellCodeEnum;
import cn.beehive.cell.base.hander.strategy.AbstractCellConfigStrategy;
import cn.beehive.cell.base.hander.strategy.ICellConfigCodeEnum;
import cn.beehive.cell.openai.enums.OpenAiChatWebCellConfigCodeEnum;
import org.springframework.stereotype.Component;

/**
 * @author hncboy
 * @date 2023/5/31
 * OpenAi 对话 Web GPT-4 配置项策略
 */
@Component
public class OpenAiChatWeb4CellConfigStrategy extends AbstractCellConfigStrategy {

    @Override
    public CellCodeEnum getCellCode() {
        return CellCodeEnum.OPENAI_CHAT_WEB_4;
    }

    @Override
    public Class<? extends ICellConfigCodeEnum> getCellConfigCodeEnumClazz() {
        return OpenAiChatWebCellConfigCodeEnum.class;
    }
}
