package cn.beehive.cell.bing.controller;

import cn.beehive.base.domain.query.RoomMsgCursorQuery;
import cn.beehive.base.handler.response.R;
import cn.beehive.cell.bing.domain.request.RoomBingMsgSendRequest;
import cn.beehive.cell.bing.domain.vo.RoomBingMsgVO;
import cn.beehive.cell.bing.service.RoomBingMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;

/**
 * @author hncboy
 * @date 2023/5/26
 * NewBing 房间控制器
 */
@AllArgsConstructor
@Tag(name = "NewBing 房间相关接口")
@RequestMapping("/room/bing")
@RestController
public class RoomBingController {

    private final RoomBingMsgService roomBingMsgService;

    @Operation(summary = "消息列表")
    @GetMapping("/list")
    public R<List<RoomBingMsgVO>> list(@Validated RoomMsgCursorQuery cursorQuery) {
        return R.data(roomBingMsgService.list(cursorQuery));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public ResponseBodyEmitter send(@Validated @RequestBody RoomBingMsgSendRequest sendRequest, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return roomBingMsgService.send(sendRequest);
    }
}