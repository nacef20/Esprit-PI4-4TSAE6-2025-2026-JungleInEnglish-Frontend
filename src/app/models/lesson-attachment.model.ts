export type AttachmentType = 'VIDEO' | 'PDF' | 'WORD' | 'PPT' | 'OTHER';

export interface LessonAttachment {
  id: number;
  lessonId: number;
  type: AttachmentType;
  originalName: string;
  contentType: string;
  size: number;
  uploadDate: string;
}