/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx.tk.quantum;

import javafx.application.Platform;

import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import com.sun.glass.ui.ClipboardAssistance;

import java.security.AccessController;
import java.security.PrivilegedAction;

class GlassSceneDnDEventHandler {

    private final GlassScene scene;

    public GlassSceneDnDEventHandler(final GlassScene scene) {
        this.scene = scene;
    }

    // Drop target handlers

    public TransferMode handleDragEnter(final int x, final int y, final int xAbs, final int yAbs,
                                        final TransferMode recommendedTransferMode,
                                        final ClipboardAssistance dropTargetAssistant)
    {
        assert Platform.isFxApplicationThread();
        return AccessController.doPrivileged(new PrivilegedAction<TransferMode>() {
            @Override
            public TransferMode run() {
                if (scene.dropTargetListener != null) {
                    QuantumClipboard dragboard =
                            QuantumClipboard.getDragboardInstance(dropTargetAssistant, false);
                    return scene.dropTargetListener.dragEnter(x, y, xAbs, yAbs,
                            recommendedTransferMode, dragboard);
                }
                return null;
            }
        }, scene.getAccessControlContext());
    }

    public void handleDragLeave(final ClipboardAssistance dropTargetAssistant) {
        assert Platform.isFxApplicationThread();
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                if (scene.dropTargetListener != null) {
                    scene.dropTargetListener.dragExit(0, 0, 0, 0);
                }
                return null;
            }
        }, scene.getAccessControlContext());
    }

    public TransferMode handleDragDrop(final int x, final int y, final int xAbs, final int yAbs,
                                       final TransferMode recommendedTransferMode,
                                       final ClipboardAssistance dropTargetAssistant)
    {
        assert Platform.isFxApplicationThread();
        return AccessController.doPrivileged(new PrivilegedAction<TransferMode>() {
            @Override
            public TransferMode run() {
                if (scene.dropTargetListener != null) {
                    return scene.dropTargetListener.drop(x, y, xAbs, yAbs,
                            recommendedTransferMode);
                }
                return null;
            }
        }, scene.getAccessControlContext());
    }

    public TransferMode handleDragOver(final int x, final int y, final int xAbs, final int yAbs,
                                       final TransferMode recommendedTransferMode,
                                       final ClipboardAssistance dropTargetAssistant)
    {
        assert Platform.isFxApplicationThread();
        return AccessController.doPrivileged(new PrivilegedAction<TransferMode>() {
            @Override
            public TransferMode run() {
                if (scene.dropTargetListener != null) {
                    return scene.dropTargetListener.dragOver(x, y, xAbs, yAbs,
                            recommendedTransferMode);
                }
                return null;
            }
        }, scene.getAccessControlContext());
    }

    // Drag source handlers

    // This is a callback from the native platform, when a drag gesture is
    // detected. This mechanism is currently not used in FX, as we have
    // a custom gesture recognizer in Scene, and DnD is started with
    // Toolkit.startDrag().
    public void handleDragStart(final int button, final int x, final int y, final int xAbs, final int yAbs,
                                final ClipboardAssistance dragSourceAssistant)
    {
        assert Platform.isFxApplicationThread();
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                if (scene.dragGestureListener != null) {
                    QuantumClipboard dragboard =
                            QuantumClipboard.getDragboardInstance(dragSourceAssistant, true);
                    scene.dragGestureListener.dragGestureRecognized(
                            x, y, xAbs, yAbs, button, dragboard);
                }
                return null;
            }
        }, scene.getAccessControlContext());
    }

    // This is a callback from the native platform, when the drag was started
    // from handleDragStart() above, or when FX as a drag source is embedded
    // to Swing/SWT.
    public void handleDragEnd(final TransferMode performedTransferMode,
                              final ClipboardAssistance dragSourceAssistant)
    {
        assert Platform.isFxApplicationThread();
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                try {
                    if (scene.dragSourceListener != null) {
                        scene.dragSourceListener.dragDropEnd(0, 0, 0, 0, performedTransferMode);
                    }
                } finally {
                    QuantumClipboard.releaseCurrentDragboard();
                }
                return null;
            }
        }, scene.getAccessControlContext());
    }

}
