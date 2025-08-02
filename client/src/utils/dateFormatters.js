import { formatDistance, formatRelative, isToday, isYesterday, format } from 'date-fns';

export const formatMessageTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  
  const date = new Date(dateTimeString);
  
  if (isToday(date)) {
    return format(date, 'p'); // 12:34 PM
  } else if (isYesterday(date)) {
    return `Yesterday, ${format(date, 'p')}`;
  } else {
    return format(date, 'MMM d, p'); // Jan 1, 12:34 PM
  }
};

export const formatChatTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  
  const date = new Date(dateTimeString);
  
  if (isToday(date)) {
    return format(date, 'p'); // 12:34 PM
  } else if (isYesterday(date)) {
    return 'Yesterday';
  } else {
    return format(date, 'MMM d'); // Jan 1
  }
};

export const formatUserLastActive = (dateTimeString) => {
  if (!dateTimeString) return 'Never';
  
  const date = new Date(dateTimeString);
  return formatDistance(date, new Date(), { addSuffix: true });
};

export const formatNotificationTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  
  const date = new Date(dateTimeString);
  return formatRelative(date, new Date());
};