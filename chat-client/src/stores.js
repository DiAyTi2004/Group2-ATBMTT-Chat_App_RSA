import AccountStore from "./components/Account/AccountStore";
import AuthStore from "./components/Auth/AuthStore";
import ChatStore from "./components/ChatV2/ChatStore";
import FriendsStore from "./components/Friends/FriendsStore";
import UserProfileStore from './components/UserProfile/UserProfileStore';
import { createContext, useContext } from "react";

const stores = {
  chatStore: new ChatStore(),
  friendsStore: new FriendsStore(),
  accountStore: new AccountStore(),
  authStore: new AuthStore(),
  userProfileStore: new UserProfileStore()
};

export const StoreContext = createContext(stores);

export function useStore() {
  return useContext(StoreContext);
}
