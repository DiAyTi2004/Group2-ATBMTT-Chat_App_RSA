import React, { useEffect, useState, memo, useRef } from 'react';
import Compose from './Compose/Compose';
import Toolbar from '../Toolbar/Toolbar';
import Message from './Message/Message';
import LocalStorage from 'src/common/LocalStorage';
import './MessageList.css';
import { object } from 'yup';
import { observer } from 'mobx-react';
import { useStore } from 'src/stores';
import { ConstructionOutlined } from '@mui/icons-material';
import MessageListLoadingSkeleton from './MessageListLoadingSkeleton';

function MessageList(props: any) {
  const ref = useRef<HTMLDivElement>(null);
  const { chatStore } = useStore();
  const {
    isLoading,
    chosenRoom
  } = chatStore;
  // const isLoading = true;
  const MY_USER_ID = LocalStorage.getLoginUser()?.username;

  const scrollToBottom = () => {
    if (ref.current) {
      ref.current.scrollTop = ref.current.scrollHeight;
      // console.log(ref.current.scrollHeight)
    }
  };

  const renderMessages = () => {
    const messages = chosenRoom?.messages || [];
    let i = 0;
    let messageCount = messages.length;
    let tempArray = [];
    while (i < messageCount) {
      let previous = messages[i - 1];
      let current = messages[i];
      let next = messages[i + 1];
      let prevType = previous && previous.messageType.name;
      let type = current.messageType.name;
      let nextType = next && next.messageType.name;
      let prevUser = previous && prevType == "chat" ? previous.user.username : null;
      let currUser = type == "chat" ? current.user.username : null;
      let nextUser = next && nextType == "chat" ? next.user.username : null;
      let isMine = current.user.username === MY_USER_ID;
      let startsSequence = true;
      let endsSequence = false;
      let photo = !isMine && current.user.avatar != null ? current.user.avatar : 'https://www.treasury.gov.ph/wp-content/uploads/2022/01/male-placeholder-image.jpeg';
      let sendDate = current.sendDate;
      if (previous && prevUser === currUser) {
        startsSequence = false
      }

      if (next && nextUser !== currUser) {
        endsSequence = true
      }

      tempArray.push(
        <Message
          key={i}
          isMine={isMine}
          type={type}
          startsSequence={startsSequence}
          endsSequence={endsSequence}
          data={current.content}
          author={current.user.username}
          photo={photo}
          sendDate={sendDate}
        />
      );
      i += 1;
    }
    return tempArray;
  }

  useEffect(function () {
    scrollToBottom();

  }, [chosenRoom?.messages?.length, chosenRoom]);

  return (
    <div className="message-list">
      <Toolbar title="Conversation Title" />

      <div className="message-list-container" id="messageListContainer" ref={ref}>
        {isLoading ? (
          <MessageListLoadingSkeleton />
        ) : (
          <>
            {!chosenRoom ? (
              <div className="no-message">No conversation was chosen</div>
            )
              :
              (
                <>
                  {
                    renderMessages()
                  }
                </>
              )
            }
          </>
        )}
      </div>
      <Compose></Compose>
    </div>
  );
}

export default memo(observer(MessageList));