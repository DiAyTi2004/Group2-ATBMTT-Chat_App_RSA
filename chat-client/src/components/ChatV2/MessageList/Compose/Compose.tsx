import React, { memo, useState } from 'react';
import './Compose.css';
import Button from '@mui/material/Button';
import SendRoundedIcon from '@mui/icons-material/SendRounded';
import { observer } from 'mobx-react';
import { useStore } from 'src/stores';

function Compose() {
  const { chatStore } = useStore();
  const { sendMessage } = chatStore;
  const [messageContent, setMessageContent] = useState("");

  function handleChangeMessageContent(event: any) {
    const { value } = event.target;
    setMessageContent(value);
  }

  function handleSendMessage() {
    sendMessage(messageContent);
  }

  return (
    <div className="compose">
      <input
        type="text"
        className="compose-input br-10 p-2 mr-2"
        placeholder="Type a message, @name"
        value={messageContent}
        onChange={handleChangeMessageContent}
      />
      <Button variant="contained" onClick={handleSendMessage}>
        <SendRoundedIcon></SendRoundedIcon>
      </Button>
    </div>
  );
}

export default memo(observer(Compose));