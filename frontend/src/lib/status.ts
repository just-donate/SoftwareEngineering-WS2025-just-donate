import { Theme } from "@/styles/themes";


export const statusTexts: Record<string, string> = {
  announced: "The donation has been announced.",
  pending_confirmation: "The donation is pending confirmation.",
  confirmed: "The donation has been confirmed.",
  received: "The donation has been received.",
  in_transfer: "The donation is in transfer.",
  processing: "The donation is being processed.",
  allocated: "The donation has been allocated.",
  awaiting_utilization: "The donation is awaiting utilization.",
  used: "The donation has been used.",
};

export const statusIds: string[] = Object.keys(statusTexts);

export const getStatusColor = (status: string, theme: Theme) => {
    console.log("STATUS", status);
    console.log(theme.statusColors);
    switch (status.toLowerCase()) {
      case 'announced':
        return theme.statusColors.announced;
      case 'pending_confirmation':
        return theme.statusColors.pending_confirmation;
      case 'confirmed':
        return theme.statusColors.confirmed;
      case 'received':
        return theme.statusColors.received;
      case 'in_transfer':
        return theme.statusColors.in_transfer;
      case 'processing':
        return theme.statusColors.processing;
      case 'allocated':
        return theme.statusColors.allocated;
      case 'awaiting_utilization':
        return theme.statusColors.awaiting_utilization;
      case 'used':
        return theme.statusColors.used;
      default:
        return theme.statusColors.announced;
    }
  };