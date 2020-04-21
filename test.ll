; ModuleID = 'test.c'
source_filename = "test.c"
target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-windows-msvc19.21.27702"

; Function Attrs: norecurse nounwind readonly uwtable
define dso_local i32 @main(i32 %0, i8** nocapture readonly %1) local_unnamed_addr #0 !dbg !10 {
  call void @llvm.dbg.value(metadata i8** %1, metadata !17, metadata !DIExpression()), !dbg !22
  call void @llvm.dbg.value(metadata i32 %0, metadata !18, metadata !DIExpression()), !dbg !22
  %3 = icmp sgt i32 %0, 0, !dbg !23
  br i1 %3, label %4, label %8, !dbg !23

4:                                                ; preds = %2
  %5 = load i8*, i8** %1, align 8, !dbg !24, !tbaa !25
  call void @llvm.dbg.value(metadata i8* %5, metadata !19, metadata !DIExpression()), !dbg !29
  %6 = load i8, i8* %5, align 1, !dbg !30, !tbaa !31
  %7 = sext i8 %6 to i32, !dbg !30
  br label %8

8:                                                ; preds = %2, %4
  %9 = phi i32 [ %7, %4 ], [ 1, %2 ], !dbg !22
  ret i32 %9, !dbg !32
}

; Function Attrs: nounwind readnone speculatable willreturn
declare void @llvm.dbg.value(metadata, metadata, metadata) #1

attributes #0 = { norecurse nounwind readonly uwtable "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "frame-pointer"="none" "less-precise-fpmad"="false" "min-legal-vector-width"="0" "no-infs-fp-math"="false" "no-jump-tables"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { nounwind readnone speculatable willreturn }

!llvm.dbg.cu = !{!0}
!llvm.module.flags = !{!5, !6, !7, !8}
!llvm.ident = !{!9}

!0 = distinct !DICompileUnit(language: DW_LANG_C99, file: !1, producer: "clang version 10.0.0 ", isOptimized: true, runtimeVersion: 0, emissionKind: FullDebug, enums: !2, retainedTypes: !3, nameTableKind: None)
!1 = !DIFile(filename: "test.c", directory: "C:\\Users\\arikr\\LangSpec", checksumkind: CSK_MD5, checksum: "f66d68893929e1da633ba76e7ab905ba")
!2 = !{}
!3 = !{!4}
!4 = !DIBasicType(name: "int", size: 32, encoding: DW_ATE_signed)
!5 = !{i32 2, !"CodeView", i32 1}
!6 = !{i32 2, !"Debug Info Version", i32 3}
!7 = !{i32 1, !"wchar_size", i32 2}
!8 = !{i32 7, !"PIC Level", i32 2}
!9 = !{!"clang version 10.0.0 "}
!10 = distinct !DISubprogram(name: "main", scope: !1, file: !1, line: 2, type: !11, scopeLine: 2, flags: DIFlagPrototyped, spFlags: DISPFlagDefinition | DISPFlagOptimized, unit: !0, retainedNodes: !16)
!11 = !DISubroutineType(types: !12)
!12 = !{!4, !4, !13}
!13 = !DIDerivedType(tag: DW_TAG_pointer_type, baseType: !14, size: 64)
!14 = !DIDerivedType(tag: DW_TAG_pointer_type, baseType: !15, size: 64)
!15 = !DIBasicType(name: "char", size: 8, encoding: DW_ATE_signed_char)
!16 = !{!17, !18, !19}
!17 = !DILocalVariable(name: "args", arg: 2, scope: !10, file: !1, line: 2, type: !13)
!18 = !DILocalVariable(name: "num", arg: 1, scope: !10, file: !1, line: 2, type: !4)
!19 = !DILocalVariable(name: "str", scope: !20, file: !1, line: 4, type: !14)
!20 = distinct !DILexicalBlock(scope: !21, file: !1, line: 3)
!21 = distinct !DILexicalBlock(scope: !10, file: !1, line: 3)
!22 = !DILocation(line: 0, scope: !10)
!23 = !DILocation(line: 3, scope: !10)
!24 = !DILocation(line: 4, scope: !20)
!25 = !{!26, !26, i64 0}
!26 = !{!"any pointer", !27, i64 0}
!27 = !{!"omnipotent char", !28, i64 0}
!28 = !{!"Simple C/C++ TBAA"}
!29 = !DILocation(line: 0, scope: !20)
!30 = !DILocation(line: 5, scope: !20)
!31 = !{!27, !27, i64 0}
!32 = !DILocation(line: 8, scope: !10)
